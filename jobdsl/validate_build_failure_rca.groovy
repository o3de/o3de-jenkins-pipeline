pipelineJob('validate_build_failure_rca') {
    definition {
        cpsScm {
            lightweight(true)
            scm {
                git {
                    branch('*/main')
                    extensions {
                        pruneStaleBranch()
                        pruneTags {
                            pruneTags(true)
                        }
                    }
                    remote {
                        credentials('o3de-ci-bot')
                        url('https://github.com/aws-lumberyard/build-failure-rca.git')
                    }
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        disableConcurrentBuilds {
            abortPrevious(false)
        }
        pipelineTriggers {
            triggers {
                pollSCM {
                    scmpoll_spec('H/5 * * * *')
                }
            }
        }
    }
}
