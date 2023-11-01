pipelineJob('O3DE-development_nightly-installer') {
    definition {
        cpsScm {
            lightweight(true)
            scm {
                git {
                    branch('development')
                    extensions {
                        pruneStaleBranch()
                        pruneTags {
                            pruneTags(true)
                        }
                    }
                    remote {
                        credentials('o3de-ci-bot')
                        url('https://github.com/o3de/o3de.git')
                    }
                }
            }
            scriptPath('scripts/build/Jenkins/Jenkinsfile')
        }
    }
    description('''
        Outputs to S3/Cloudfront address: 

        <tr><a href="https://o3debinaries.org/development/Latest/Windows/o3de_installer.exe">https://o3debinaries.org/development/Latest/Windows/o3de_installer.exe</a></tr>
        <tr><a href="https://o3debinaries.org/development/Latest/Linux/O3DE_latest.deb">https://o3debinaries.org/development/Latest/Linux/O3DE_latest.deb</a></tr>
        <tr><a href="https://o3debinaries.org/2.0.0/Linux/o3de_2.0.0_amd64.snap">https://o3debinaries.org/2.0.0/Linux/o3de_2.0.0_amd64.snap</a></tr>
    '''.stripIndent().trim())
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        authorizationMatrix {
            entries{
                group{
                    name('o3de*aws-ops')
                    permissions([
                        'Credentials/Create',
                        'Credentials/Delete',
                        'Credentials/ManageDomains',
                        'Credentials/Update',
                        'Credentials/View',
                        'Job/Build',
                        'Job/Cancel',
                        'Job/Configure',
                        'Job/Delete',
                        'Job/Discover',
                        'Job/Move',
                        'Job/Read',
                        'Job/Workspace',
                        'Run/Delete',
                        'Run/Replay',
                        'Run/Update',
                        'SCM/Tag'
                    ])
                }
            }
            inheritanceStrategy {
                nonInheriting()
            }
        }
        disableConcurrentBuilds {
            abortPrevious(false)
        }
        pipelineTriggers {
            triggers {
                pollSCM {
                    scmpoll_spec('30 5 * * 1-6')
                }
            }
        }
    }
    quietPeriod(120)
}
