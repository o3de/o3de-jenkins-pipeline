pipelineJob('O3DE-development_nightly-installer') {
    definition {
        cpsScm {
            lightweight(true)
            scm {
                git {
                    branch('development')
                    extensions {
                        pruneStaleBranch()
                        pruneTags {
                            pruneTags(true)
                        }
                    }
                    remote {
                        credentials('github-access-token')
                        url('https://github.com/o3de/o3de.git')
                    }
                }
            }
            scriptPath('scripts/build/Jenkins/Jenkinsfile')
        }
    }
    description('''
        Outputs to S3/Cloudfront address: 
        <tr><a href>https://o3debinaries.org/development/Latest/Windows/o3de_installer.exe</a></tr>
        <tr><a href>https://o3debinaries.org/development/Latest/Linux/O3DE_latest.deb</a></tr>
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
                permissions([
                    'GROUP:hudson.model.Item.Read:o3de*aws',
                    'GROUP:hudson.model.Item.Workspace:o3de*aws',
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
                    'GROUP:hudson.scm.SCM.Tag:o3de*aws-ops'
                ])
            }
        }
        disableConcurrentBuilds {
            abortPrevious(false)
        }
        pipelineTriggers {
            triggers {
                pollSCM {
                    scmpoll_spec('15 8 * * 1-6')
                }
            }
        }
    }
    quietPeriod(120)
}
