multibranchPipelineJob('MARS METRICS') {
    branchSources {
        branchSource {
            source {
                github {
                    id('MARS-METRICS-GitHub')
                    configuredByUrl(false)
                    credentialsId('github-access-token')
                    repoOwner('aws-lumberyard')
                    repository('o3de-mars')
                    repositoryUrl('https://github.com/aws-lumberyard/o3de-mars.git')
                    traits {
                        authorInChangelogTrait()
                        gitHubBranchDiscovery {
                            strategyId(1)
                        }
                        pruneStaleBranchTrait()
                        pruneStaleTagTrait()
                    }
                }
            }
            strategy {
                allBranchesSame {
                    props {
                        suppressAutomaticTriggering {
                            triggeredBranchesRegex('^$')
                        }
                    }
                }
            }
        }
    }
    displayName('MARS Metrics')
    factory {
        workflowBranchProjectFactory {
            scriptPath('Jenkinsfile')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
            numToKeep(100)
        }
    }
}
