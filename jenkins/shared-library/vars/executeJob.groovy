def call(String amlJobExecutionScript, String amlJobSetCommand, Boolean streamJob = false) {
  sh """
      streamOption=''
      if [[ $streamJob ]]; then
          echo 'Run the AzureML job and wait for the job to finish.'
          streamOption=--stream
      fi

      job_name="CI_${BUILD_NUMBER}"
      az ml job create -f ${amlJobExecutionScript} ${amlJobSetCommand} --name \$job_name \${streamOption}
      status=\$(az ml job show -n \$job_name --query status -o tsv)

      if [[ \$status != 'Completed' ]]
      then
        echo "Training Job failed"
        exit 1
      fi
      """
}
