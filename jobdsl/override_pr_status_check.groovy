pipelineJob('override-pr-status-check') {
    definition {
        cps {
            sandbox(true)
            script('''
                #!/usr/bin/env groovy
                import groovy.json.JsonOutput
                properties([
                    parameters([
                        string(name: 'PR_URL', defaultValue: '', description: 'URL of the PR to override status check rule')
                    ])
                ])
                stage('Override PR Status Check') {
                    node('controller') {
                        echo "Overriding status check rule for ${env.PR_URL}"
                        message_json = [
                            "pr_url": env.PR_URL,
                            "maintainer": currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
                        ]
                        snsPublish(
                            topicArn: 'arn:aws:sns:us-west-2:206706238973:override-pr-status-check',
                            subject:'PR Check Rule Override Request',
                            message:JsonOutput.toJson(message_json)
                        )
                    }
                }
            ''')
        }
    }
    logRotator {
        daysToKeep(365)
    }
    parameters {
        stringParam('PR_URL', '', 'URL of the PR to override status check rule')
    }
    properties {
        authorizationMatrix {
            entries{
                group{
                    name('o3de')
                    permissions([
                        'Job/Read'
                    ])
                }
                group{
                    name('o3de*sig-chairs')
                    permissions([
                        'Job/Build',
                        'Job/Configure',
                        'Job/Read'
                    ])
                }
            }
            inheritanceStrategy {
                nonInheriting()
            }
        }
        disableConcurrentBuilds {
            abortPrevious(false)
        }
    }
}
