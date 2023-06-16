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
from copy import copy
from pipeline import JenkinsPipeline, MissingContextError


ACCOUNT = "123456789012"
REGION = "us-west-2"
BRANCH = "test"
CERT_ARN = f"arn:aws:acm:{REGION}:{ACCOUNT}:certificate/certificate_id"
CODESTAR_CONNECTION = f"arn:aws:codestar-connections:{REGION}:{ACCOUNT}:connection/codestar_id"
REPO = "o3de/repo"
VPC_ID = "vpc-01234567890"

TEST_CONTEXT = {
    "branch": BRANCH,
    "cert-arn": CERT_ARN,
    "codestar-connection": CODESTAR_CONNECTION,
    "repo": REPO,
    "vpc-id": VPC_ID
  }


@pytest.fixture
def template():
    app = cdk.App(context=TEST_CONTEXT)
    jenkins_pipeline_stack = JenkinsPipeline(app, 'JenkinsPipelineStack', env=cdk.Environment(account=ACCOUNT, region=REGION))

    return Template.from_stack(jenkins_pipeline_stack)


def test_stack_context_values(template):
    """Test that the provided context is correctly passed to the resource properties"""
    template.has_resource_properties("AWS::CodePipeline::Pipeline", {
        "Stages": Match.array_with([
            Match.object_like(
                {
                    "Actions": [
                        {
                            "Configuration": {
                                "ConnectionArn": CODESTAR_CONNECTION,
                                "FullRepositoryId": REPO,
                                "BranchName": BRANCH
                            }
                        }
                    ]
                }
            )
        ])
    })

    template.has_resource_properties("AWS::IAM::Policy", {
        "PolicyDocument": {
            "Statement": Match.array_with([
                {
                    "Action": "codestar-connections:UseConnection",
                    "Effect": "Allow",
                    "Resource": CODESTAR_CONNECTION
                }
            ])
        }
    })


def test_required_resources(template):
    template.resource_count_is("AWS::CodePipeline::Pipeline", 1)

    # Verify the deploy stage is created
    template.has_resource_properties("AWS::CodePipeline::Pipeline", {
        "Stages": Match.array_with([
            Match.object_like(
                {
                    "Actions": Match.array_with([
                        Match.object_like(
                            {
                                "ActionTypeId": {
                                    "Category": "Approval",
                                    "Owner": "AWS",
                                    "Provider": "Manual",
                                },
                                "ActionTypeId": {
                                    "Category": "Deploy",
                                    "Owner": "AWS",
                                    "Provider": "CloudFormation",
                                }
                            }
                        )
                    ])
                }
            )
        ])
    })


def test_missing_context_error():
    """An exception should be raised if a required context value is missing."""
    test_missing_context = copy(TEST_CONTEXT)
    del test_missing_context['branch']
    with pytest.raises(MissingContextError):
        app = cdk.App(context=test_missing_context)
        JenkinsPipeline(app, 'JenkinsPipelineStack', env=cdk.Environment(account=ACCOUNT, region=REGION))
