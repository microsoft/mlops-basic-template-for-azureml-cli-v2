def call(String clusterName, String computeSize = 'STANDARD_D4S_V3',
         Integer minInstances = 0, Integer maxInstances = 4, String clusterTier = 'dedicated') {
  echo('================ Creating compute in AzureML ================')
  sh """
          az ml compute create --name ${clusterName} \
                                  --type amlcompute \
                                  --size ${computeSize} \
                                  --min-instances ${minInstances} \
                                  --max-instances ${maxInstances} \
                                  --tier ${clusterTier}
    """
         }
