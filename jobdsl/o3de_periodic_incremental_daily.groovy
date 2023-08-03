multibranchPipelineJob('O3DE_periodic-incremental-daily') {
    branchSources {
        branchSource {
            source {
                github {
                    id('O3DE_periodic-incremental-daily')
                    configuredByUrl(false)
                    credentialsId('o3de-ci-bot')
                    repoOwner('o3de')
                    repository('o3de')
                    repositoryUrl('https://github.com/o3de/o3de.git')
                    traits {
                        authorInChangelogTrait()
                        gitHubBranchDiscovery {
                            strategyId(3)
                        }
                        pruneStaleBranchTrait()
                        pruneStaleTagTrait()
                    }
                }
            }
            strategy {
                namedBranchesDifferent {
                    defaultProperties {
                        suppressAutomaticTriggering {
                            triggeredBranchesRegex('^$')
                        }
                    }
                }
            }
        }
    }
    displayName('O3DE [Periodic Incremental Daily]')
    factory {
        workflowBranchProjectFactory {
            scriptPath('scripts/build/Jenkins/Jenkinsfile')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
            numToKeep(14)
        }
    }
    properties {
        authorizationMatrix {
            inheritanceStrategy {
                inheriting()
            }
            permissions([
                'USER:hudson.model.Item.Read:anonymous'
            ])
        }
    }
}
