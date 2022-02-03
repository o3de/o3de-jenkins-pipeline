#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

import os
import sys
import aws_cdk.core as cdk

from jenkins_server.jenkins_server_stack import JenkinsServerStack, MissingContextError

# Account and region set by the default AWS profile or one specified with --profile
ACCOUNT = os.environ.get('CDK_DEFAULT_ACCOUNT')
REGION = os.environ.get('CDK_DEFAULT_REGION')

app = cdk.App()

try:
    cdk_env = app.node.try_get_context("cdk-env")  # Optional prefix for stackname (e.g. prod)
    JenkinsServerStack(app, 'JenkinsServerStack',
        stack_name=f'{cdk_env}JenkinsServerStack',
        env=cdk.Environment(account=ACCOUNT, region=REGION)
    )
except FileNotFoundError:
    print('Error loading configs. Verify files exists.')
    sys.exit(1)
except MissingContextError:
    print('Error getting required context value for stack. Please provide one using --context key=value')
    sys.exit(1)

app.synth()
