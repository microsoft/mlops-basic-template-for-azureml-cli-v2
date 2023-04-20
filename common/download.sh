#/bin/sh
JOB_NAME=$1
WORKSPACE=$2
RESOURCE_GROUP=$3
OUTPUT_NAME=$4
SUB_JOB_NAME=$(az ml job list --parent-job-name $JOB_NAME --query "[?display_name=='train_job'].name" -o tsv --resource-group $RESOURCE_GROUP --workspace-name $WORKSPACE)    
az ml job download --name ${SUB_JOB_NAME//[$'\r']} --resource-group $RESOURCE_GROUP --workspace-name $WORKSPACE --output-name $OUTPUT_NAME