# this is the repository makefile

-include .mlops/makefile

ifneq ($(ENV_FILE),)
include $(ENV_FILE)
else
include config.env
endif

MLOPS_REPO_URL?=https://github.com/Mandur/mlops-basic-template-for-azureml-cli-v2.git
MLOPS_REPO_VERSION=feature/add-makefile
##################################################################
## Project specific targets
##################################################################
train: train
##################################################################
## ML Ops targets (DO NOT MODIFY)
##################################################################


.PHONY: init
.PHONY: test
updateTools=false
ifeq ($(updateTools),true)
init: -setup-mlops
else ifeq ($(wildcard .mlops/makefile),)
init: -setup-mlops
else
init:
endif

..PHONY: -setup-mlops
-setup-mlops:
	@echo Running ML Ops setup...
	@git clone $(MLOPS_REPO_URL) mlops-temp --quiet
	@cd mlops-temp && git checkout $(MLOPS_REPO_VERSION) --quiet
ifeq ($(OS),Windows_NT)
	@IF EXIST .mlops RMDIR /S /Q .mlops
	@mkdir .mlops && xcopy mlops-temp\common .mlops\ /E/H/Y/Q && rmdir mlops-temp /S /Q
else
	@mkdir -p .mlops && cp -r mlops-temp/.mlops .
	@rm -rf mlops-temp 
endif
	@echo Done!