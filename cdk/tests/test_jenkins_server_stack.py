#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

import aws_cdk as cdk
import pytest

from aws_cdk.assertions import Match, Template
from jenkins_server.jenkins_server import JenkinsServerStack
from test_jenkins_pipeline_stack import CERT_ARN, TEST_CONTEXT


ACCOUNT = "123456789012"
REGION = "us-west-2"
VPC_ID = "None"  # Used to test that a VPC is still created if one is not provided


@pytest.fixture
def template():
    app = cdk.App(context=TEST_CONTEXT)
    jenkins_server_stack = JenkinsServerStack(app, 'JenkinsServerStack', 
                                                env=cdk.Environment(account=ACCOUNT, region=REGION),
                                                tags={
                                                    'cert-arn': CERT_ARN,
                                                    'vpc-id': VPC_ID
                                                }
                                              )

    return Template.from_stack(jenkins_server_stack)


def test_stack_context_values(template):
    """Test that the provided context is correctly passed to the resource properties"""
    template.has_resource_properties("AWS::ElasticLoadBalancingV2::Listener", {
        "Certificates": [
            {
            "CertificateArn": CERT_ARN
            }
        ]
    })


def test_required_resources(template):
    template.resource_count_is("AWS::EC2::VPC", 1)
    template.resource_count_is("AWS::SNS::Topic", 1)
    template.resource_count_is("AWS::Logs::LogGroup", 1)
    template.resource_count_is("AWS::EFS::FileSystem", 1)
    template.resource_count_is("AWS::EFS::AccessPoint", 1)
    template.resource_count_is("AWS::ECS::Cluster", 1)
    template.resource_count_is("AWS::ECS::Service", 1)
    template.resource_count_is("AWS::ECS::TaskDefinition", 1)

    # Security group for EFS
    template.has_resource_properties("AWS::EC2::SecurityGroupIngress", {
        "FromPort": 2049,
        "ToPort": 2049
    })

    # Security group for Fargate task
    template.has_resource_properties("AWS::EC2::SecurityGroupIngress", {
        "FromPort": 8080,
        "ToPort": 8080
    })

    # Security group for load balancer
    template.has_resource_properties("AWS::EC2::SecurityGroup", {
        "SecurityGroupIngress": Match.array_with([
            Match.object_like(
                {
                    "FromPort": 443,
                    "IpProtocol": "tcp",
                    "ToPort": 443
                }
            )
        ])
    })

    template.has_resource_properties("AWS::ElasticLoadBalancingV2::Listener", {
        "Port": 443,
        "Protocol": "HTTPS"
    })


def test_permissions(template):
    # Test pass role permission is limited to EC2
    template.has_resource_properties("AWS::IAM::Policy", {
        "PolicyDocument": {
            "Statement": Match.array_with([
                {
                    "Action": "iam:PassRole",
                    "Condition": {
                        "StringEquals": {
                            "iam:PassedToService": "ec2.amazonaws.com"
                        }
                    },
                    "Effect": "Allow",
                    "Resource": "*"
                }
            ])

        }
    })

    # Test S3 bucket does not grant public access
    template.has_resource_properties("AWS::S3::Bucket", {
        "PublicAccessBlockConfiguration": {
            "BlockPublicAcls": True,
            "BlockPublicPolicy": True,
            "IgnorePublicAcls": True,
            "RestrictPublicBuckets": True
        }
    })
