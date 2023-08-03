pipelineJob('O3DE-test_nightly-installer') {
    definition {
        cpsScm {
            lightweight(true)
            scm {
                git {
                    branch('core20-snap-2305-point-fixes')
                    extensions {
                        pruneStaleBranch()
                        pruneTags {
                            pruneTags(true)
                        }
                    }
                    remote {
                        credentials('o3de-ci-bot')
                        url('https://github.com/aws-lumberyard-dev/o3de.git')
                    }
                }
            }
            scriptPath('scripts/build/Jenkins/Jenkinsfile')
        }
    }
    description('''
        Outputs to S3/Cloudfront address: 

        <tr><a href="https://o3debinaries.org/0.0.40/Linux/o3de_0.0.40_amd64.snap">https://o3debinaries.org/0.0.40/Linux/o3de_0.0.40_amd64.snap</a></tr>
    '''.stripIndent().trim())
    displayName('O3DE [Nightly Installer] Development')
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        authorizationMatrix {
            inheritanceStrategy {
                nonInheriting()
            }
            permissions([
                'GROUP:hudson.model.Item.Read:o3de*aws',
                'GROUP:hudson.model.Item.Workspace:o3de*aws',
                'GROUP:hudson.model.Item.Read:o3de*maintainers',
                'GROUP:hudson.model.Item.Workspace:o3de*maintainers',
                'GROUP:com.cloudbees.plugins.credentials.CredentialsProvider.Create:o3de*aws-ops',
                'GROUP:com.cloudbees.plugins.credentials.CredentialsProvider.Delete:o3de*aws-ops',
                'GROUP:com.cloudbees.plugins.credentials.CredentialsProvider.ManageDomains:o3de*aws-ops',
                'GROUP:com.cloudbees.plugins.credentials.CredentialsProvider.Update:o3de*aws-ops',
                'GROUP:com.cloudbees.plugins.credentials.CredentialsProvider.View:o3de*aws-ops',
                'GROUP:hudson.model.Item.Build:o3de*aws-ops',
                'GROUP:hudson.model.Item.Cancel:o3de*aws-ops',
                'GROUP:hudson.model.Item.Configure:o3de*aws-ops',
                'GROUP:hudson.model.Item.Delete:o3de*aws-ops',
                'GROUP:hudson.model.Item.Discover:o3de*aws-ops',
                'GROUP:hudson.model.Item.Move:o3de*aws-ops',
                'GROUP:hudson.model.Item.Read:o3de*aws-ops',
                'GROUP:hudson.model.Item.Workspace:o3de*aws-ops',
                'GROUP:hudson.model.Run.Delete:o3de*aws-ops',
                'GROUP:hudson.model.Run.Replay:o3de*aws-ops',
                'GROUP:hudson.model.Run.Update:o3de*aws-ops',
                'GROUP:hudson.scm.SCM.Tag:o3de*aws-ops',
                'USER:hudson.model.Item.Build:AMZN-Phil',
                'USER:hudson.model.Item.Cancel:AMZN-Phil',
                'USER:hudson.model.Item.Configure:AMZN-Phil',
                'USER:hudson.model.Item.Read:AMZN-Phil',
                'USER:hudson.model.Item.Workspace:AMZN-Phil',
                'USER:hudson.model.Item.Build:spham-amzn',
                'USER:hudson.model.Item.Cancel:spham-amzn',
                'USER:hudson.model.Item.Configure:spham-amzn',
                'USER:hudson.model.Item.Read:spham-amzn',
                'USER:hudson.model.Item.Workspace:spham-amzn'
            ])
        }
        disableConcurrentBuilds {
            abortPrevious(false)
        }
        pipelineTriggers {
            triggers {
                pollSCM {
                    scmpoll_spec('30 5 * * 1-6')
                }
            }
        }
    }
    quietPeriod(120)
}
