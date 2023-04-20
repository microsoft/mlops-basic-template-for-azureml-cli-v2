# MLOps Basic Template for Azure ML CLI v2

> **Note:**
> This is a repo that can be shared to our customers. This means it's NOT OK to include Microsoft confidential
> content. All discussions should be appropriate for a public audience.

## About this repo

The idea of this template is to provide a minimum number of scripts to implement development environment to train new models using Azure ML CLI v2 and the CI CD pipeline tool of your choice: Azure Devops or Jenkins.

The template contains the following folders/files:

- devops: the folder contains Azure DevOps related files (yaml files to define Builds).
- jenkins: the folder contains Jenkins related files.
- docs: documentation.
- src: source code that is not related to Azure ML directly. Usually, there is data science related code.
- mlops: scripts that are related to Azure ML.
- mlops/nyc-taxi: a fake pipeline with some basic code.
- .amlignore: using this file we are removing all the folders and files that are not supposed to be in Azure ML compute.
- test: unit tests
- common: common components that can be reused by different projects. This folder should be moved in its own private repository as explained in [the pattern page]().

The template contains the following documents:

- docs/how_to_setup.md: explain how to configure the template.

## How to use the repo

Information about how to setup the repo is in [the following document](./docs/how_to_setup.md).
