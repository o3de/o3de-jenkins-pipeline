multibranchPipelineJob('o3de-extras') {
    branchSources {
        branchSource {
            source {
                github {
                    id('o3de-extras')
                    configuredByUrl(false)
                    credentialsId('o3de-ci-bot')
                    repoOwner('o3de')
                    repository('o3de-extras')
                    repositoryUrl('https://github.com/o3de/o3de-extras.git')
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
                    namedExceptions {
                        named {
                            name('main')
                        }
                        named {
                            name('development')
                        }
                        named {
                            name('stabilization/*')
                        }
                    }
                }
            }
        }
    }
    factory {
        remoteJenkinsFileWorkflowBranchProjectFactory {
            fallbackBranch('development')
            localMarker('')
            remoteJenkinsFileSCM {
                gitSCM {
                    userRemoteConfigs {
                        userRemoteConfig {
                            url('https://github.com/o3de/o3de.git')
                            name('')
                            refspec('')
                            credentialsId('o3de-ci-bot')
                        }
                        branches {
                            branchSpec {
                                name('*/development')
                            }
                        }
                        browser {}
                        gitTool(null)
                    }
                }
            }
            scriptPath('scripts/build/Jenkins/Jenkinsfile')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
            numToKeep(14)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('2m')
        }
    }
}
