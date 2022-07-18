#! /bin/bash -e
#
# Copyright (c) Contributors to the Open 3D Engine Project.
# For complete copyright and license terms please see the LICENSE at the root of this distribution.
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#
#

# Remove plugin directory to delete stale data. Required if $JENKINS_HOME is on a shared filesystem.
# Also ignore symlinks. This is another option to relocate directories off the shared filesystem.
plugin_folder="$JENKINS_HOME/plugins"

if [ -d $plugin_folder ] && [ ! -L $plugin_folder ]; then
    echo Found plugins folder. Removing...
    rm -rf $plugin_folder
fi

# Call original entrypoint using source to preserve command line arguments
. /usr/local/bin/jenkins.sh
