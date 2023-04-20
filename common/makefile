ifneq ($(ENV_FILE),)
include $(ENV_FILE)
else
include config.env
endif

train:
	echo [CLOUD] Training with a job pipeline on compute=$(COMPUTE_NAME)
	az ml job create --set settings.default_compute="azureml:$(COMPUTE_NAME)" \
	    --file $(JOB_FILE) \
	    --resource-group $(RESOURCE_GROUP) --workspace-name $(WORKSPACE) \
	    $(CUSTOM_XARGS) $(private_xargs) $(jenkins_args) 
	@echo Queued training job



create-environment:
	@echo creating environment from $(TRAINING_ENVIRONMENT_FILE)
	@az ml environment create --file $(TRAINING_ENVIRONMENT_FILE) --resource-group $(RESOURCE_GROUP) --workspace-name $(WORKSPACE)
	@echo Environment created

download-model:
	@echo Downloading trained model from output $(OUTPUT_NAME) of job $(JOB_NAME)
	bash ./.mlops/download.sh $(JOB_NAME) $(WORKSPACE) $(RESOURCE_GROUP) $(OUTPUT_NAME)
	@echo Downloading trained model completed

get-job:
	@az ml job show --name $(JOB_NAME) -g $(RESOURCE_GROUP) -w $(WORKSPACE)

test:
	@echo Running Unit Test
	@pytest $(TEST_PATH) 
	@echo Unit Test completed