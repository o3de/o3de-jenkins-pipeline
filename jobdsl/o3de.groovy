multibranchPipelineJob('O3DE') {
    branchSources {
        branchSource {
            source {
                github {
                    id('O3DE-GitHub')
                    configuredByUrl(false)
                    credentialsId('github-access-token')
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
    description('''
        <p>Source for fully featured AAA Open 3D Engine</p>
        <div>Latest AR runs: <a href="https://jenkins.build.o3de.org/blue/organizations/jenkins/O3DE/branches/">O3DE Branch Activity/</a></div>
        <div>Last finished build status: <a href="https://jenkins.build.o3de.org/job/O3DE/job/development/lastStableBuild/">Last Stable Build</a></div>
    '''.stripIndent().trim())
    displayName('O3DE')
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
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
}
