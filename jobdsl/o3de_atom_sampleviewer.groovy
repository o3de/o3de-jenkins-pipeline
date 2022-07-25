multibranchPipelineJob('o3de-atom-sampleviewer') {
    branchSources {
        branchSource {
            source {
                github {
                    id('o3de-atom-sampleviewer-GitHub')
                    configuredByUrl(false)
                    credentialsId('github-access-token')
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
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
        }
    }
    displayName('Atom SampleViewer')
    factory {
        workflowBranchProjectFactory {
            scriptPath('Scripts/build/Jenkins/Jenkinsfile')
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
            interval('5m')
        }
    }
}
