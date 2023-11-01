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
    configure {
        def traits = it / 'sources' / 'data' / 'jenkins.branch.BranchSource' / 'source' / 'traits'
        traits << 'org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait' {
            strategyId(1)
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustEveryone')
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
            remoteJenkinsFile('scripts/build/Jenkins/Jenkinsfile')
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
            entries{
                user{
                    name('anonymous')
                    permissions([
                        'Job/Read'
                    ])
                }
            }
            inheritanceStrategy {
                inheriting()
            }
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('2m')
        }
    }
}
