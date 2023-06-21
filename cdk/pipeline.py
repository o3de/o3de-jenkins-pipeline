#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

import aws_cdk.aws_codebuild as codebuild
import aws_cdk.aws_iam as iam
import aws_cdk.pipelines as pipelines

from aws_cdk import Environment, Stack, Stage
from constructs import Construct
from jenkins_server.jenkins_server import JenkinsServerStack


class MissingContextError(Exception):
    pass


class JenkinsServer(Stage):
    """Stage wrapper for the Jenkins server stack."""
    def __init__(self, scope: Construct, id: str, cert_arn: str, vpc_id: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        JenkinsServerStack(self, 'JenkinsServerStack',
            tags={
                'cert-arn': cert_arn,
                'vpc-id': vpc_id
            }
        )


class JenkinsPipeline(Stack):
    """Defines the CDK pipeline to deploy the Jenkins server stack.

    Parameters are passed to the pipeline through context values. An exception is raised if a required parameter
    is missing.

    """

    def __init__(self, scope: Construct, id: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)
        self.codestar_connection = self._get_required_context('codestar-connection')
        self.repo = self._get_required_context('repo')
        self.branch = self._get_required_context('branch')
        self.cert_arn = self._get_required_context('cert-arn')
        self.vpc_id = self._get_optional_context('vpc-id')
        self.source = pipelines.CodePipelineSource.connection(self.repo, self.branch, connection_arn=self.codestar_connection)

        self._create_pipeline()

    def _get_required_context(self, context_name):
        """Get context value and raise an exception if it does not exist."""
        context_value = self.node.try_get_context(context_name)
        if context_value is None:
            print(f'Required context missing: {context_name}')
            raise MissingContextError
        return context_value
    
    def _get_optional_context(self, context_name):
        """Get context value and set it to 'None' if it does not exist. Some constructs cannot have a null or empty string ID."""
        return self.node.try_get_context(context_name) or 'None'

    def _create_pipeline(self):
        pipeline = pipelines.CodePipeline(self, 'Pipeline',
            synth=pipelines.CodeBuildStep('Synth',
                input=self.source,
                commands=[
                    'cd cdk',
                    'npm install -g aws-cdk',
                    'pip install -r requirements.txt',
                    f'cdk synth --verbose \
                        --context codestar-connection={self.codestar_connection} \
                        --context repo={self.repo} \
                        --context branch={self.branch} \
                        --context cert-arn={self.cert_arn} \
                        --context vpc-id={self.vpc_id}'
                ],
                build_environment=codebuild.BuildEnvironment(
                    build_image=codebuild.LinuxBuildImage.STANDARD_5_0
                ),
                primary_output_directory='cdk/cdk.out',
                role_policy_statements=[
                    iam.PolicyStatement(
                        actions=["sts:AssumeRole"],
                        resources=["*"],
                        conditions={
                            "StringEquals": {
                                "aws:ResourceTag/aws-cdk:bootstrap-role": "lookup"
                            }
                        }
                    )
                ]
            )
        )

        cdk_tests = pipelines.CodeBuildStep('CDKTests',
            input=self.source,
            commands=[
                'cd cdk',
                'pip install -r requirements.txt',
                'python -m pytest -v'
            ],
            build_environment=codebuild.BuildEnvironment(
                build_image=codebuild.LinuxBuildImage.STANDARD_5_0
            )
        )

        pipeline.add_stage(
            JenkinsServer(self, self.branch,
                cert_arn=self.cert_arn,
                vpc_id=self.vpc_id,
                env=Environment(account=self.account, region=self.region)
            ),
            pre=[
                cdk_tests,
                pipelines.ManualApprovalStep('ReleaseToProd')
            ]
        )
