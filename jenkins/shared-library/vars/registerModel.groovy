def call(String pipelineName, String modelName, String jobDisplayName) {
  def String trainingJob = sh(script: "az ml job list --parent-job-name $pipelineName --query \"[?display_name=='$jobDisplayName'].name\" -o tsv", returnStdout: true).trim()
  return "azureml:$modelName:${sh(script: "az ml model create --name $modelName --path azureml://jobs/${trainingJob}/outputs/artifacts/paths/model/ --type mlflow_model --query version -o tsv", returnStdout: true).trim()}"
}
