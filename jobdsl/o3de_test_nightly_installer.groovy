pipelineJob('O3DE-test_nightly-installer') {
    definition {
        cpsScm {
            lightweight(true)
            scm {
                git {
                    branch('core20-snap-2305-point-fixes')
                    extensions {
                        pruneStaleBranch()
                        pruneTags {
                            pruneTags(true)
                        }
                    }
                    remote {
                        credentials('o3de-ci-bot')
                        url('https://github.com/aws-lumberyard-dev/o3de.git')
                    }
                }
            }
            scriptPath('scripts/build/Jenkins/Jenkinsfile')
        }
    }
    description('''
        Outputs to S3/Cloudfront address: 

        <tr><a href="https://o3debinaries.org/0.0.40/Linux/o3de_0.0.40_amd64.snap">https://o3debinaries.org/0.0.40/Linux/o3de_0.0.40_amd64.snap</a></tr>
    '''.stripIndent().trim())
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        authorizationMatrix {
            entries{
                group{
                    name('o3de*aws')
                    permissions([
                        'Job/Read',
                        'Job/Workspace'
                    ])
                }
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
                user{
                    name('AMZN-Phil')
                    permissions([
                        'Job/Build',
                        'Job/Cancel',
                        'Job/Configure',
                        'Job/Read',
                        'Job/Workspace'
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
