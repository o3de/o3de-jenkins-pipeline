pipelineJob('default-development_snapshot') {
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
    description('Creates snapshots at regular intervals')
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
    }
    quietPeriod(120)
}
