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
            entries{
                group{
                    name('o3de*aws-ops')
                    permissions([
                        'Credentials/Create',
                        'Credentials/Delete',
                        'Credentials/ManageDomains',
                        'Credentials/Update',
                        'Credentials/View',
                        'Job/Build',
                        'Job/Cancel',
                        'Job/Configure',
                        'Job/Delete',
                        'Job/Discover',
                        'Job/Move',
                        'Job/Read',
                        'Job/Workspace',
                        'Run/Delete',
                        'Run/Replay',
                        'Run/Update',
                        'SCM/Tag'
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
            entries{
                group{
                    name('o3de*aws-ops')
                    permissions([
                        'Credentials/Create',
                        'Credentials/Delete',
                        'Credentials/ManageDomains',
                        'Credentials/Update',
                        'Credentials/View',
                        'Job/Build',
                        'Job/Cancel',
                        'Job/Configure',
                        'Job/Delete',
                        'Job/Discover',
                        'Job/Move',
                        'Job/Read',
                        'Job/Workspace',
                        'Run/Delete',
                        'Run/Replay',
                        'Run/Update',
                        'SCM/Tag'
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
            entries{
                group{
                    name('o3de*aws-ops')
                    permissions([
                        'Credentials/Create',
                        'Credentials/Delete',
                        'Credentials/ManageDomains',
                        'Credentials/Update',
                        'Credentials/View',
                        'Job/Build',
                        'Job/Cancel',
                        'Job/Configure',
                        'Job/Delete',
                        'Job/Discover',
                        'Job/Move',
                        'Job/Read',
                        'Job/Workspace',
                        'Run/Delete',
                        'Run/Replay',
                        'Run/Update',
                        'SCM/Tag'
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
