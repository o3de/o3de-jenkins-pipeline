multibranchPipelineJob('o3de-netsoaktest') {
    branchSources {
        branchSource {
            source {
                github {
                    id('o3de-netsoaktest')
                    configuredByUrl(false)
                    credentialsId('o3de-ci-bot')
                    repoOwner('o3de')
                    repository('o3de-netsoaktest')
                    repositoryUrl('https://github.com/o3de/o3de-netsoaktest.git')
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
    configure {
        def traits = it / 'sources' / 'data' / 'jenkins.branch.BranchSource' / 'source' / 'traits'
        traits << 'org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait' {
            strategyId(1)
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustEveryone')
        }
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath('Scripts/build/Jenkins/Jenkinsfile')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
            numToKeep(28)
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
    triggers {
        periodicFolderTrigger {
            interval('2m')
        }
    }
}
