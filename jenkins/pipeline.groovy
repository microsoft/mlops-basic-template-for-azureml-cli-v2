library 'shared-library@feature/register-model'
pipeline {
    agent any

    parameters {
        string(name: 'ENVIRONMENT', defaultValue: 'demo', description: 'Environment name. Will be used to resolve the config file.')
        booleanParam(name: 'WAIT_FOR_TRAINING', defaultValue: false, description: 'If the pipeline should wait for the end of the training. And check the status.')
        booleanParam(name: 'DEPLOY_INFRASTRUCTURE', defaultValue: true, description: 'Whether to deploy the required AzureML compute infrastructure')
        booleanParam(name: 'CREATE_NEW_ENVIRONMENT', defaultValue: true, description: 'Deploy the required AzureML compute infrastructure')
    }

    environment {
        // If jenkins pipeline was triggered by a pull request, wait for the job to finish
        STREAM_JOB = "${(env.BRANCH_NAME).startsWith('PR-') ? true : params.WAIT_FOR_TRAINING}"
    }

    stages {
        stage('Build validation pipeline') {
            agent {
                docker {
                    image 'python:3.8'
                    args "--user root --privileged --env-file $JENKINS_HOME/workspace/ci_dev_pipeline/jenkins/environment/${params.ENVIRONMENT}.env"
                }
            }
            steps {
                script {
                    buildValidationPipeline()
                }
            }
            post {
                always {
                    sh('echo "================ publish test results ================"')
                    junit 'junit/test-results.xml'
                }
                success {
                    sh('echo "================ publishing coverage results ================"')
                    cobertura coberturaReportFile: 'coverage.xml'
                }
            }
        }
        stage('Execute Job on AzureML') {
            agent {
                docker {
                    image 'mcr.microsoft.com/azure-cli:latest'
                    args "--user root --privileged --env-file $WORKSPACE/jenkins/environment/${params.ENVIRONMENT}.env"
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

                stage('Create AzureML compute') {
                    when { expression { return params.DEPLOY_INFRASTRUCTURE } }
                    steps {
                        script {
                            createCompute('$CLUSTER_NAME')
                        }
                    }
                }

                stage('Create AzureML environment') {
                    when { expression { return params.CREATE_NEW_ENVIRONMENT } }
                    steps {
                        script {
                            createEnvironment('$ENVIRONMENT_NAME', './mlops/nyc-taxi/environment.yml')
                        }
                    }
                }

                stage('Execute AzureML job') {
                    steps {
                        script {
                            executeJob('./mlops/nyc-taxi/pipeline.yml', '--set experiment_name=$EXPERIMENT_NAME \
                                    settings.default_compute=azureml:$CLUSTER_NAME \
                                    jobs.prep_job.environment=azureml:$ENVIRONMENT_NAME@latest \
                                    jobs.transform_job.environment=azureml:$ENVIRONMENT_NAME@latest \
                                    jobs.train_job.environment=azureml:$ENVIRONMENT_NAME@latest \
                                    jobs.predict_job.environment=azureml:$ENVIRONMENT_NAME@latest \
                                    jobs.score_job.environment=azureml:$ENVIRONMENT_NAME@latest \
                                    display_name=$DISPLAY_NAME',
                                    env.STREAM_JOB.toBoolean()
                                    )
                        }
                    }
                }
            }
        }
    }
}
