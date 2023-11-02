#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

import os
import sys
import aws_cdk as cdk

from cdk_nag import AwsSolutionsChecks
from pipeline import JenkinsPipeline, MissingContextError

# Account and region set by the default AWS profile or one specified with --profile
ACCOUNT = os.environ.get('CDK_DEFAULT_ACCOUNT')
REGION = os.environ.get('CDK_DEFAULT_REGION')

app = cdk.App()
cdk.Aspects.of(app).add(AwsSolutionsChecks(verbose=True))

try:
    JenkinsPipeline(app, 'JenkinsPipelineStack', env=cdk.Environment(account=ACCOUNT, region=REGION))
except FileNotFoundError:
    print('Error loading configs. Verify files exists.')
    sys.exit(1)
except MissingContextError:
    print('Error getting required context value for stack. Please provide one using --context or cdk.context.json.')
    sys.exit(1)

app.synth()
