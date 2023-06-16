#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

import json
import aws_cdk.aws_ec2 as ec2
import aws_cdk.aws_ecs as ecs
import aws_cdk.aws_ecr_assets as ecr_assets
import aws_cdk.aws_efs as efs
import aws_cdk.aws_elasticloadbalancingv2 as elb
import aws_cdk.aws_iam as iam
import aws_cdk.aws_logs as logs
import aws_cdk.aws_sns as sns
import aws_cdk.aws_s3 as s3

from os import path
from aws_cdk import Stack
from constructs import Construct


CONFIG_FILE = 'stack_config.json'


class JenkinsServerStack(Stack):
    """Defines a stack to host the Jenkins server in ECS.

    Stack core components:
        - ECS/Fargate: Hosts the Jenkins server container
        - EFS: Stores the Jenkins home directory
        - ALB: Load balancer to route traffic to the Fargate task

    The options for each component are stored in the stack config file.
    """

    def __init__(self, scope: Construct, id: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)
        self.stack_tags = self.tags.tag_values()
        self.stack_config = self._load_stack_config(CONFIG_FILE)
        self.cert_arn = self._load_cert_arn()

        self.vpc = self._create_vpc()
        self.build_topic = sns.Topic(self, 'BuildTopic')
        self.log_group = logs.LogGroup(self, 'LogGroup')
        self.file_system, self.access_point = self._create_efs()
        self.fargate_service = self._create_ecs()
        self._create_alb()

    def _load_stack_config(self, config_file):
        """Load stack config. The config file is expected to be in the same directory."""
        stack_directory = path.dirname(path.abspath(__file__))
        config_file_path = path.join(stack_directory, config_file)
        try:
            with open(config_file_path) as f:
                return json.load(f)
        except FileNotFoundError:
            print(f'Config file cannot be found: {config_file}')
            raise

    def _load_cert_arn(self):
        """Load cert arn. If cdk context does not exist pull from tags (stack is being deployed via pipelines)."""
        cert_arn = self.node.try_get_context('cert-arn')
        if cert_arn is None:
            cert_arn = self.stack_tags['cert-arn']
        return cert_arn
    
    def _create_vpc(self):
        """Create a new VPC or use an existing one if a VPC ID is provided."""
        if self.stack_tags['vpc-id'] == 'None':  # Context values will be converted to string and cannot be empty during synth
            return ec2.Vpc(self, 'VPC',
                cidr=self.stack_config['vpc']['cidr'],
                nat_gateways=self.stack_config['vpc']['nat_gateways']
            )
        return ec2.Vpc.from_lookup(self, 'VPC', vpc_id=self.stack_tags['vpc-id'])

    def _create_efs(self):
        """Create a file system with an access point for the jenkins home directory."""
        efs_config = self.stack_config['efs']

        file_system = efs.FileSystem(self, 'FileSystem',
            vpc=self.vpc,
            enable_automatic_backups=True,
            encrypted=True
        )

        access_point = file_system.add_access_point('AccessPoint',
            create_acl=efs.Acl(
                owner_gid=efs_config['user_id'],
                owner_uid=efs_config['user_id'],
                permissions=efs_config['permissions']
            ),
            path=efs_config['access_point_path'],
            posix_user=efs.PosixUser(
                gid=efs_config['user_id'],
                uid=efs_config['user_id']
            )
        )
        return file_system, access_point

    def _create_ecs(self):
        """Create the ECS cluster to host the Jenkins server container."""
        ecs_config = self.stack_config['ecs']

        cluster = ecs.Cluster(self, 'EcsCluster',
            container_insights=True,
            vpc=self.vpc
        )

        fargate_task_def = ecs.FargateTaskDefinition(self, 'TaskDef',
            cpu=ecs_config['task']['cpu'],
            memory_limit_mib=ecs_config['task']['memory'],
        )

        fargate_task_def.add_volume(
            name=ecs_config['container']['volume_name'],
            efs_volume_configuration=ecs.EfsVolumeConfiguration(
                file_system_id=self.file_system.file_system_id,
                authorization_config=ecs.AuthorizationConfig(
                    access_point_id=self.access_point.access_point_id
                ),
                transit_encryption='ENABLED'
            )
        )

        # ARN specific IAM resources mapped here
        resource_map = {
            "arn.log_group": self.log_group.log_group_arn,
            "arn.sns": self.build_topic.topic_arn
        }

        # Add permissions to task IAM role from the stack config file
        for permission in ecs_config['task_role_permissions'].values():
            print(permission['resources'])
            if permission['resources'].startswith('arn.'):
                # Replace ARN placeholder with mapped value
                resources = resource_map[permission['resources']]
            else:
                resources = permission['resources']
            fargate_task_def.add_to_task_role_policy(iam.PolicyStatement(
                actions=permission['actions'],
                resources=[resources],
                conditions=permission.get('conditions')
            ))

        port = ecs_config['service']['application_port']

        # Container image is created from the dockerfile at the root of the repo and uploaded to ECR
        container = fargate_task_def.add_container('JenkinsContainer',
            image=ecs.ContainerImage.from_docker_image_asset(
                ecr_assets.DockerImageAsset(self, 'JenkinsDockerImage', directory='..')
            ),
            logging=ecs.LogDriver.aws_logs(
                stream_prefix='jenkins',
                log_group=self.log_group
            ),
            port_mappings=[ecs.PortMapping(container_port=port, host_port=port)],
        )

        mount_point_config = ecs_config['container']['mount_point']
        container.add_mount_points(ecs.MountPoint(
            container_path=mount_point_config['container_path'],
            read_only=mount_point_config['read_only'],
            source_volume=mount_point_config['source_volume']
        ))

        ulimits = ecs_config['container']['ulimits']
        for ulimit in ulimits:
            container.add_ulimits(ecs.Ulimit(
                name=ecs.UlimitName(ulimit['name']),
                hard_limit=ulimit['hard_limit'],
                soft_limit=ulimit['soft_limit']
            ))

        fargate_service = ecs.FargateService(self, 'FargateService',
            task_definition=fargate_task_def,
            assign_public_ip=True,
            platform_version=ecs.FargatePlatformVersion.VERSION1_4,
            cluster=cluster,
            desired_count=ecs_config['service']['desired_count']
        )

        self.file_system.connections.allow_default_port_from(fargate_service)
        return fargate_service

    def _create_alb(self):
        """Create a load balancer with targets for the ECS task."""
        alb_config = self.stack_config['alb']

        alb = elb.ApplicationLoadBalancer(self, 'ALB',
            vpc=self.vpc,
            deletion_protection=True,
            internet_facing=True
        )

        health_check = alb_config['health_check']
        for name, port in alb_config['ports'].items():
            listener = alb.add_listener(name,
                certificates=[elb.ListenerCertificate(self.cert_arn)],
                port=port,
                ssl_policy=elb.SslPolicy(alb_config['ssl_policy'])
            )
            listener.add_targets(name,
                health_check=elb.HealthCheck(
                    path=health_check['path'],
                    healthy_threshold_count=health_check['healthy_threshold_count'],
                    unhealthy_threshold_count=health_check['unhealthy_threshold_count']
                ),
                port=self.stack_config['ecs']['service']['application_port'],
                targets=[self.fargate_service],
            )

        alb.log_access_logs(
            s3.Bucket(self, 'AccessLogsBucket',
                block_public_access=s3.BlockPublicAccess.BLOCK_ALL,
                encryption=s3.BucketEncryption.S3_MANAGED
            )
        )

        # Adds default redirect from http to https
        alb.add_redirect()

        if alb_config['public'] is True:
            alb.connections.allow_from_any_ipv4(ec2.Port.tcp(443))
