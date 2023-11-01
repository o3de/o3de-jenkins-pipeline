multibranchPipelineJob('o3de-atom-sampleviewer') {
    branchSources {
        branchSource {
            source {
                github {
                    id('o3de-atom-sampleviewer')
                    configuredByUrl(false)
                    credentialsId('o3de-ci-bot')
                    repoOwner('o3de')
                    repository('o3de-atom-sampleviewer')
                    repositoryUrl('https://github.com/o3de/o3de-atom-sampleviewer.git')
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
    displayName('Atom SampleViewer')
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
