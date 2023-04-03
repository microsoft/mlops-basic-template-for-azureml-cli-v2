library 'shared-library'
pipeline {
    agent any

    parameters {
        string(name: 'ENVIRONMENT', defaultValue: 'demo', description: 'Environment name. Will be used to resolve the config file.')
        string(name: 'AZURE_ML_PIPELINE_NAME', defaultValue: 'jenkins_pipeline_20', description: 'the AzureML pipeline name from where to register the model.')
    }

    environment {
        // If jenkins pipeline was triggered by a pull request, wait for the job to finish
        STREAM_JOB = "${(env.BRANCH_NAME).startsWith('PR-') ? true : params.WAIT_FOR_TRAINING}"
    }

    stages {
        stage('deploy Model on AzureML') {
            agent {
                docker {
                    image 'mcr.microsoft.com/azure-cli:latest'
                    args "--user root --env-file $WORKSPACE/jenkins/environment/${params.ENVIRONMENT}.env"
                }
            }

            stages {
                stage('Configure AzureML agent') {
                    steps {
                        script {
                            configureAzureMLAgent()
                        }
                    }
                }

                stage('Connect to AzureML workspace') {
                    steps {
                        script {
                            connectToWorkspace('$RESOURCE_GROUP', '$WORKSPACE_NAME')
                        }
                    }
                }

                stage('Register Model') {
                    steps {
                        script {
                            env.MODEL_PATH = registerModel("${params.AZURE_ML_PIPELINE_NAME}", '$MODEL_NAME', '$JOB_DISPLAY_NAME')
                        }
                    }
                }
                stage('Deploy to online endpoint') {
                    when {
                        expression {
                            def containerName = sh(script: 'echo $ENDPOINT_NAME', returnStdout: true)
                            return containerName.trim() != ''
                        }
                    }
                    steps {
                        script {
                            deployToOnlineEndpoint('$ENDPOINT_NAME', "${env.MODEL_PATH}", "${ENVIRONMENT}-CI-${BUILD_NUMBER}", 'managed')
                        }
                    }
                }
                stage('Deploy to Kubernetes Compute') {
                    when {
                        expression {
                            def containerName = sh(script: 'echo $KUBERNETES_ENDPOINT_NAME', returnStdout: true)
                            return containerName.trim() != ''
                        }
                    }
                    steps {
                        script {
                            deployToOnlineEndpoint('$KUBERNETES_ENDPOINT_NAME', "${env.MODEL_PATH}", "${ENVIRONMENT}-CI-${BUILD_NUMBER}", 'kubernetes')
                        }
                    }
                }
            }
        }
    }
}
