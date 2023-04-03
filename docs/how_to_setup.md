# How to setup the repo

This template supports Azure ML as a platform for ML, and Azure DevOps or Jenkins as a platform for operationalization. Therefore, we assume that you already have an Azure ML Workspace as well as an Azure DevOps project in place, and all the code from the repository has been cloned into the DevOps project.

## Using Azure DevOps

In order to setup the repository, you need to complete few steps.

**Step 1.** Create a service connection in Azure DevOps. You can use [this document](https://learn.microsoft.com/en-us/azure/devops/pipelines/library/service-endpoints?view=azure-devops&tabs=yaml) as a reference. Use Azure Resource Manager as a type of the service connection.

**Step 2.** Create a new variable group with the following variables:

- EXPERIMENT_BASE_NAME: an experiment base name. This parameter as well as two more parameters below we are using as a background to form unique names for experiments, runs and models. You can find a rule for the names in [this template](../devops/pipeline/templates/experiment_variables.yml). By default we are using the branch name as well as build id to form the names that helps us to differentiate experiments, runs and models working in a big team of data scientists and software engineers. The EXPERIMENT_TYPE variable from the template is hard coded in _dev_pipeline.yml files.
- DISPLAY_BASE_NAME: a run base name (see EXPERIMENT_BASE_NAME for details).
- MODEL_BASE_NAME: a model base name (see EXPERIMENT_BASE_NAME for details).
- AZURE_RM_SVC_CONNECTION: the service connection name from the previous step.
- WORKSPACE_NAME: an Azure ML workspace name.
- RESOURCE_GROUP: a resource group where Azure Ml Workspace is located.
- CLUSTER_NAME: an Azure ML compute cluster name to start jobs.
- CLUSTER_SIZE: a size of the cluster in Azure ML to start jobs.
- CLUSTER_REGION: a location/region where the cluster should be created.

Information about variable groups in Azure DevOps can be found in [this document](https://learn.microsoft.com/en-us/azure/devops/pipelines/library/variable-groups?view=azure-devops&tabs=classic).

**Step 3.** Create a *development* branch and make it as default one to make sure that all PRs should go towards to it. This template assumes that the team works at a *development* branch as a primary source for coding and improving the model quality. Later, you can implement Azure Pipeline that mode code from the *development* branch into qa/main or execute a release process right away. Release management is not in scope of this template.

**Step 4.** Create two Azure Pipelines. Both Azure Pipelines should be created based on existing YAML files. The first one is based on the [pr_to_dev_pipeline.yml](../devops/pipeline/pr_to_dev_pipeline.yml), and it helps to maintain code quality for all PRs including integration tests for the Azure ML experiment. Usually, we recommend to have a toy dataset for the integration tests to make sure that the Azure ML job can be completed fast enough - there is no a goal to check model quality and we just need to make sure that our job can be executed. The second Azure Pipeline is based on [ci_dev_pipeline.yml](../devops/pipeline/ci_dev_pipeline.yml) that should be executed automatically once new PR has been merged into the *development* branch. The main idea of this pipeline is to execute training on the full dataset to generate a model that can be a candidate for production. This Azure Pipeline should be extended based on the project's requirements. 

More details about how to create a basic Azure Pipeline can be found [here](https://learn.microsoft.com/en-us/azure/devops/pipelines/create-first-pipeline?view=azure-devops&tabs).

**Step 5.** Setup a policy for the *development* branch. At this stage we have an Azure Pipeline that should be executed on every PR to the *development* branch. At the same time successful completion of the build is not a requirement. So, it's important to add our PR build as a policy. Pay special attention that [pr_to_dev_pipeline.yml](../devops/pipeline/pr_to_dev_pipeline.yml) has various paths in place. We are using these paths to limit number of runs if the current PR doesn't affect ML component (for example, PR modifies a documentation file). Therefore, setting up the policy you need to make sure that you are using the same set of paths in the *Path filter* field.

More details about how to create a policy can be found [here](https://learn.microsoft.com/en-us/azure/devops/repos/git/branch-policies?view=azure-devops&tabs=browser).

**Step 6. (Optional)** It's a common practice to execute training job on the full dataset once PR has been merged into the development branch. At the same time, the training process can take much time (many hours or even days) and Azure DevOps agent will not be able to let you know about the status due to timeout settings. So, it's very hard to implement a single CI Build that is waiting for a new model (training results) and execute other steps after that (model approval, model movement into qa environment, model deployment etc).

Azure ML provides a solution that allows us to implement a *server* task in Azure DevOps Build and wait for the result of the pipeline training job with no Azure DevOps agent holding. Thanks to that it's possible to wait for results any amount of time and execute all other steps right after completion of the Azure ML training job. As for now, the feature is in active development, but you can [visit this link](https://github.com/Azure/azure-mlops-automation) to check the status and find how to get access. This new Azure ML feature can be included in your CI Build thanks to the extension that Azure ML team built or you can use RestAPITask for a direct REST call. In this template we implemented both options, and you can pick any.

To activate option 1, you need to uncomment it in `ci_dev_pipeline.yml`.

To activate option 2, you need to uncomment it in `ci_dev_pipeline.yml`, and you need to setup a new **Generic** service connection with **Server URL** parameter in the following format `https://{azure ml workspace location}.experiments.azureml.net/webhook/v1.0/` and add its name into the variable group as **AML_REST_CONNECTION**.

Now, you can create a PR and test the flow.

**Step 7. (Optional)** The default Azure ML pipeline used in this repository is built using hard-coded jobs, but optionally you may wish to implement a [component](https://learn.microsoft.com/en-us/azure/machine-learning/concept-component) based pipeline. Components are self contained pieces of code that can be reused across multiple pipelines - which can be useful in customers who have many different pipelines that share common tasks.

To review these components, explore the `mlops\nyc-taxi\components` folder, and the YAML within, which defines each component. In this use case, each component maps directly to one of the jobs in the default pipeline, and in the `mlops\nyc-taxi\pipeline_components.yml` pipeline, they are executed in the same order.

If you want to test out the component based pipeline, simply comment out the reference to `mlops\nyc-taxi\pipeline.yml` in the `ci_dev_pipeline.yml` and `pr_to_def_pipeline.yml` and replace it with `mlops\nyc-taxi\pipeline_components.yml`, then re-run your Azure Pipelines.

For more information about components, please see the official docs [here](https://learn.microsoft.com/en-us/azure/machine-learning/concept-component).

## Using Jenkins

All the jenkins dependencies are hosted in the [jenkins folder](../jenkins/). 
The Jenkins pipeline is coded in the [pipeline.groovy](../jenkins/pipeline.groovy) file. 

All the pipeline steps are built as [Jenkins shared libraries](https://www.jenkins.io/doc/book/pipeline/shared-libraries/) in order to foster reuse. In case multiple pipelines are in scope we recommend splitting the shared libraries in another git repository and reference them from the different pipelines to avoid code duplication.

The current guide is tailored toward Github, but the approach here below can also be used using other source control and changing the Jenkins plugin (e.g. [Gitlab](https://plugins.jenkins.io/gitlab-branch-source/), [Bitbucket](https://plugins.jenkins.io/cloudbees-bitbucket-branch-source/))

> At the time being the Jenkins pipeline don't support waiting for a long AzureML training job (as the Azure DevOps pipeline) and any merge to the main branch won't be waited when ran against the full dataset.

Jenkins supports registering models and deploying them to online endpoints (managed or on Kubernetes). This additional behavior is coded at the moment in [another pipeline](../jenkins/deployModel.groovy), as we don't have a solution to wait for the AML training to be finished for now. 

> When using a Kubernetes endpoint target, following this [tutorial](https://learn.microsoft.com/azure/machine-learning/how-to-attach-kubernetes-anywhere) is required for the pipeline to work. The current example expects the Kubernetes to be added to Azure Machine Learning with the name 'kubernetes-compute'

### Prerequisite

The Jenkins pipeline expects an [Azure Service principal](https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal) to be created with the role 'Contributor' on your AzureML workspace. We expect then the following [credentials](https://www.jenkins.io/doc/book/using/using-credentials/) to be provisioned in Jenkins:

* sp-app-id to your service principal application id
* sp-tenant-id to your service principal tenant id
* sp-password to your [service principal password](https://learn.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret)

### Required Jenkins plugins

The pipeline require the install of the [cobertura plugin](https://plugins.jenkins.io/cobertura/) in your Jenkins instance. 

We recommend the usage of the [multibranch plugin](https://plugins.jenkins.io/github-branch-source/) for jenkins. In case you are using GitHub with MFA protection, a GitHub application will be needed to communicate between Jenkins and GitHub, this can be done using this [documentation](https://github.com/jenkinsci/github-branch-source-plugin/blob/master/docs/github-app.adoc).

### Setting up the Jenkins repository

In order to configure the plugin, create a new multibranch pipeline (new item -> multibranch pipeline). Follow the [Github Branch Source plugin documentation](https://docs.cloudbees.com/docs/cloudbees-ci/latest/cloud-admin-guide/github-branch-source-plugin), with the following particular settings:

* in 'Build configuration':
    * Mode: 'By JenkinsFile'
    * Script path: 'jenkins/pipeline.groovy'
* In order for the pipeline to be able to use the shared libraries. Click on "add" in the 'pipeline library' section with following settings:
    * Name: shared-library
    * Project repository: Url of your github forked repo (in a git clone format)
    * Library Path: 'jenkins/shared-library/'

To set up the inference deployment pipeline repeat the steps described here above and replace Script path by 'jenkins/deployModel.groovy'.

### Setting up the environment

The pipeline expects target environments to be populated in a file named <env_name>.env located in the [environment folder](../jenkins/environments). The file is structured as follow:

``` .env
RESOURCE_GROUP=<name of your Azure Machine learning workspace's resource group>
WORKSPACE_NAME=<name of your Azure Machine learning workspace>
EXPERIMENT_NAME=<name of an experiment>
DISPLAY_NAME=<display name for the Azure Machine learning job that will be triggered>
MODEL_NAME=<name of the Azure Machine Learning model>
CLUSTER_NAME=<the Azure Machine Learning cluster name where the training is going to be performed>
ENVIRONMENT_NAME=<name of the Azure Machine environment that will be generated>
```

Some additional parameters are required to enable the inference deployment

``` .env
ENDPOINT_NAME=<OPTIONAL, deploy the inference to an online inference endpoint>
KUBERNETES_ENDPOINT_NAME=<OPTIONAL, deploy the inference to an inference endpoint to an attached Kubernetes cluster>
```

> In case the shared libraries are published as part of an external repository we still expect the environment files to stay in the repositories where the main code is hosted (and not in the shared library location).

### Running the pipelines

The pipeline defined [above](#setting-up-the-jenkins-repository) can now be run, It will automatically execute on PRs and on pushes on any repo branch. You can change the behavior by tailoring the Jenkins Source branch plugin. It is also possible to manually execute each pipeline with the following parameters:

#### Training pipeline

The pipeline takes the following arguments when run manually:
* ENVIRONMENT: Describing which environment file will be loaded for this particular Jenkins run
* WAIT_FOR_TRAINING: Describing if the pipeline should wait until the training job is finished.
* DEPLOY_INFRASTRUCTURE: Boolean to indicate whether the pipeline should create the AzureML training infrastructure. Our recommendation would be to create and manage it via an external IaC script (e.g. terraform).
* CREATE_NEW_ENVIRONMENT: Boolean to indicate whether to build a new environment on every run. It should not be required unless library or version changed.

> We recommend to create environments outside of the Jenkins pipeline in a dedicated infrastructure as code, using for example, [Terraform](https://www.terraform.io/) or [Bicep](https://learn.microsoft.com/azure/azure-resource-manager/bicep/overview?tabs=bicep). 

#### Inference pipeline

The pipeline takes the following arguments:
* ENVIRONMENT: Describing which environment file will be loaded for this particular Jenkins run
* AZURE_ML_PIPELINE_NAME: The AzureML Pipeline run which trained the model we want to register and deploy

### Test the online endpoints

In order to test the deployed online endpoint, it is possible to [test the endpoints using the AML studio](https://learn.microsoft.com/en-us/azure/machine-learning/how-to-use-managed-online-endpoint-studio#test). In order to get a prediction here is a payload you can use:

``` json
{"data":[[0.83,40.69454575,-73.97611237,1,40.69383621,-73.97611237,0,2,2,3,1,21,2,35,2,3,1,21,5,52]]}
```

The number meanings are in the following order:

distance, dropoff_latitude, dropoff_longitude, passengers, pickup_latitude,  pickup_longitude, store_forward, vendor, pickup_weekday, pickup_month, pickup_monthday, pickup_hour, pickup_minute, pickup_second, dropoff_weekday, dropoff_month, dropoff_monthday, dropoff_hour, dropoff_minute, dropoff_second
