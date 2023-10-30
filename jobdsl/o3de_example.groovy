multibranchPipelineJob('O3DE-Example') {
    branchSources {
        branchSource {
            source {
                github {
                    id('O3DE-Example')
                    configuredByUrl(false)
                    repoOwner('o3de')
                    repository('o3de')
                    repositoryUrl('https://github.com/o3de/o3de.git')
                    traits {
                        authorInChangelogTrait()
                        gitHubBranchDiscovery {
                            strategyId(3)
                        }
                        gitHubPullRequestDiscovery {
                            strategyId(1)
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
    description('Example pipeline job for O3DE, a full-featured, real-time open source 3D engine.')
    displayName('O3DE Example Pipeline')
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
}
