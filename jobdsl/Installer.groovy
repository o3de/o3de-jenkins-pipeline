folder('Installer')

pipelineJob('Installer/Start_CloudHSM') {
    definition {
        cps {
            sandbox(true)
            script('''
                pipeline {
                    agent { label 'controller' }
                    stages {
                        stage('Start CloudHSM') {
                            steps {
                                script {
                                    sh(script: "aws lambda invoke  --function-name ${Lambda_Function} --payload '' --log-type Tail -", returnStdout: true).trim()
                                }
                                timeout(time: 10, unit: 'MINUTES') {
                                    waitUntil(initialRecurrencePeriod: 10000) {
                                        script {
                                            def resp = sh(script: "aws cloudhsmv2 describe-clusters --filters clusterIds=${CloudHSM_ClusterID}", returnStdout: true).trim()
                                            def respObj = readJSON text: resp
                                            def state = respObj["Clusters"][0]["Hsms"][0]["State"]
                                            echo "CloudHSM is ${state}"
                                            return(state ==~ "ACTIVE")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ''')
        }
    }
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        authorizationMatrix {
            inheritanceStrategy {
                nonInheriting()
                permissions([
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
                cron {
                    spec('0 5 * * 1-6')
                }
            }
        }
    }
    parameters {
        stringParam('Lambda_Function', 'o3de-cloudhsm-scaling-AddHSM-ytk7PUSOQieS', '')
        stringParam('CloudHSM_ClusterID', 'cluster-dyx3hbibkmd', '')
    }
}

pipelineJob('Installer/Start_CloudHSM_Manual') {
    definition {
        cps {
            sandbox(true)
            script('''
                pipeline {
                    agent { label 'controller' }
                    stages {
                        stage('Start CloudHSM') {
                            steps {
                                script {
                                    sh(script: "aws lambda invoke  --function-name ${Lambda_Function} --payload '' --log-type Tail -", returnStdout: true).trim()
                                }
                                timeout(time: 10, unit: 'MINUTES') {
                                    waitUntil(initialRecurrencePeriod: 10000) {
                                        script {
                                            def resp = sh(script: "aws cloudhsmv2 describe-clusters --filters clusterIds=${CloudHSM_ClusterID}", returnStdout: true).trim()
                                            def respObj = readJSON text: resp
                                            def state = respObj["Clusters"][0]["Hsms"][0]["State"]
                                            echo "CloudHSM is ${state}"
                                            return(state ==~ "ACTIVE")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ''')
        }
    }
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        authorizationMatrix {
            inheritanceStrategy {
                nonInheriting()
                permissions([
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
    }
    parameters {
        stringParam('Lambda_Function', 'o3de-cloudhsm-scaling-AddHSM-ytk7PUSOQieS', '')
        stringParam('CloudHSM_ClusterID', 'cluster-dyx3hbibkmd', '')
    }
}

pipelineJob('Installer/Stop_CloudHSM') {
    definition {
        cps {
            sandbox(true)
            script('''
                pipeline {
                    agent { label 'controller' }
                    stages {
                        stage('Stop CloudHSM') {
                            steps {
                                script {
                                    sh(script: "aws lambda invoke  --function-name ${Lambda_Function} --payload '' --log-type Tail -", returnStdout: true) 
                                }
                            }
                        }
                    }
                }
            ''')
        }
    }
    logRotator {
        daysToKeep(7)
        numToKeep(14)
    }
    properties {
        authorizationMatrix {
            inheritanceStrategy {
                nonInheriting()
                permissions([
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
                cron {
                    spec('30 12 * * 1-6')
                }
            }
        }
    }
    parameters {
        stringParam('Lambda_Function', 'o3de-cloudhsm-scaling-DeleteHSM-LGvGj6eypnpL', '')
    }
}
