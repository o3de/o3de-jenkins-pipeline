#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

import aws_cdk.aws_iam as iam
import aws_cdk.pipelines as pipelines

from aws_cdk.core import Environment, Construct, Stack, Stage
from jenkins_server.jenkins_server import JenkinsServerStack


class MissingContextError(Exception):
    pass


class JenkinsServer(Stage):
    """Stage wrapper for the Jenkins server stack."""
    def __init__(self, scope: Construct, id: str, cert_arn: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        JenkinsServerStack(self, f'{id}Stack',
            tags={
                'cert-arn': cert_arn
            }
        )


class JenkinsPipeline(Stack):
    """Defines the CDK pipeline to deploy the Jenkins server stack.

    Parameters are passed to the pipeline through context values. An exception is raised if a required parameter
    is missing.

    A staging stack is deployed if the staging-cert-arn context is provided.

    """

    def __init__(self, scope: Construct, id: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)
        self.codestar_connection = self._get_required_context('codestar-connection')
        self.repo = self._get_required_context('repo')
        self.branch = self._get_required_context('branch')
        self.prod_cert_arn = self._get_required_context('prod-cert-arn')
        self.staging_cert_arn = self.node.try_get_context('staging-cert-arn')

        self._create_pipeline()

    def _get_required_context(self, context_name):
        """Get context value and raise an exception if it does not exist."""
        context_value = self.node.try_get_context(context_name)
        if context_value is None:
            print(f'Required context missing: {context_name}')
            raise MissingContextError
        return context_value

    def _create_pipeline(self):
        pipeline = pipelines.CodePipeline(self, 'Pipeline',
            synth=pipelines.CodeBuildStep('Synth',
                input=pipelines.CodePipelineSource.connection(self.repo, self.branch,
                    connection_arn=self.codestar_connection
                ),
                commands=[
                    'cd cdk',
                    'npm install -g aws-cdk',
                    'python3 -m pip install -r requirements.txt',
                    f'cdk synth --verbose \
                        --context codestar-connection={self.codestar_connection} \
                        --context repo={self.repo}  \
                        --context branch={self.branch} \
                        --context prod-cert-arn={self.prod_cert_arn} \
                        --context staging-cert-arn={self.staging_cert_arn}'
                ],
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

        if self.staging_cert_arn is not None:
            pipeline.add_stage(
                JenkinsServer(self, 'staging',
                    cert_arn=self.staging_cert_arn,
                    env=Environment(account=self.account, region=self.region)
                )
            )

        pipeline.add_stage(
            JenkinsServer(self, 'prod',
                cert_arn=self.prod_cert_arn,
                env=Environment(account=self.account, region=self.region)
            ),
            pre=[
                pipelines.ManualApprovalStep('ReleaseToProd')
            ]
        )
