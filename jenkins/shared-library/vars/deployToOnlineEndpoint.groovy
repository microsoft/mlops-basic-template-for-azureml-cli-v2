def call(String amlResourceGroup, String amlWorkspaceName, String endpointName, String modelPath, String deploymentName, String fileNamePrefix, String onlineEndpointFileLocation, String inferenceCodeLocation) {
  sh """
        #!/bin/bash
        existingEndpoint=\$(az ml online-endpoint list  --output tsv --resource-group $amlResourceGroup --workspace-name $amlWorkspaceName --query "[?name=='$endpointName'].name | length(@)")
        if [ \$existingEndpoint -eq "0" ]; then az ml online-endpoint create --name $endpointName -f $WORKSPACE/$onlineEndpointFileLocation/$fileNamePrefix-endpoint.yml; fi
        az ml online-deployment create --all-traffic --endpoint-name $endpointName --file $WORKSPACE/$onlineEndpointFileLocation/$fileNamePrefix-online-endpoint.yml --set model=$modelPath --set endpoint_name=$endpointName --set code_configuration.code=$WORKSPACE/$inferenceCodeLocation --set environment.conda_file=$WORKSPACE/$onlineEndpointFileLocation/conda-inference.yml --set name=$deploymentName
      """
}
