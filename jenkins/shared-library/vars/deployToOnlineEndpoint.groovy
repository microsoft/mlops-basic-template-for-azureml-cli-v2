def call(String endpointName, String modelPath, String deploymentName, String fileNamePrefix) {
  sh """
        #!/bin/bash
        existingEndpoint=\$(az ml online-endpoint list  --output tsv --resource-group aml-finish --workspace-name aml-mikou --query "[?name=='$endpointName'].name | length(@)")
        if [ \$existingEndpoint -eq "0" ]; then az ml online-endpoint create --name ${endpointName} -f $WORKSPACE/mlops/nyc-taxi/${fileNamePrefix}-endpoint.yml; fi
        az ml online-deployment create --all-traffic --endpoint-name ${endpointName} --file $WORKSPACE/mlops/nyc-taxi/${fileNamePrefix}-online-endpoint.yml --set model=$modelPath --set endpoint_name=$endpointName --set code_configuration.code=$WORKSPACE/src/nyc_src/inference/ --set environment.conda_file=$WORKSPACE/mlops/nyc-taxi/conda-inference.yml --set name=$deploymentName
      """
}
