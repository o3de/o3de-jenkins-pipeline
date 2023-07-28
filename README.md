# O3DE Jenkins Pipeline

This project automates the deployment of the O3DE Jenkins Pipeline that runs the checks on all pull requests submitted to the [Open 3D Engine (O3DE)](https://github.com/o3de/o3de).

Jenkins Configuration Components:
- Dockerfile: Generates a custom image from the [Jenkins Docker Image](https://github.com/jenkinsci/docker).
- Jenkins Configuration as Code (JCasC): This automates the configuration of the Jenkins server.
- Job DSL: Pipeline jobs defined using Job DSL will be configured automatically using JCasC.
- Plugins Manager: The plugin installation manager tool provided in the parent image will install the plugins defined in the repo. 

Hosting Components:

- AWS Cloud Development Kit (CDK) Stacks: This will setup stacks using CodePipeline and ECS to host Jenkins and automate its deployment.
## Jenkins Docker Image

We use the [Jenkins Docker Image](https://github.com/jenkinsci/docker) as the parent in our `dockerfile` to generate our custom image. This allows us to install our required plugins and load other custom options.

## JCasC

Jenkins is configured using the JCasC plugin. This allows the settings to be defined using yaml files stored in the repo and prevents users from having to manually configure Jenkins on startup. 

These files should be located in the `configs` directory so that they are found by the plugin.. There is a main `jenkins.yaml` file that contains the recommended default settings. You can customize your Jenkins setup by editing this file. 

There are also template files that can be used for other optional settings. The plugin also supports having multiple yaml files and will locate all files having the .yml/.yaml extention.

## Job DSL

JCasC provides support for the Job DSL plugin to automatically configure pipeline jobs. It's recommended to store the config files in the `jobdsl` directory at the root of the project and reference them in `configs/jobs.yaml`.

Directory Structure:
```
PROJECT_ROOT/
|
|- configs/jobs.yaml
|
|- jobdsl/
    |
    |- job1.groovy
    |- job2.groovy
```

Example jobs.yaml file:
```
jobs:
  - file: ${JENKINS_LOCAL}/jobdsl/job1.groovy
  - file: ${JENKINS_LOCAL}/jobdsl/job2.groovy
```

Note: The `jobdsl` directroy is copied into $JENKINS_LOCAL when the docker image is created.

To get started the basic Job DSL API reference can be found here: https://jenkinsci.github.io/job-dsl-plugin/ The complete API reference which includes all installed plugins can be found on your Jenkins server: `$JENKINS_URL/plugin/job-dsl/api-viewer/index.html`

## Installing Plugins

The list of plugins installed on the Jenkins server is maintained in the `plugins.txt` file. Plugins added/removed from this list will be installed/uninstalled when a new image is created and deployed. 

## Running your own Jenkins Pipeline

This Jenkins Pipeline can be used to run the AR on your O3DE projects. Create a fork of this repo to customize the setup. 

### Requirements:
- Create a GitHub OAuth App

The Jenkins server is setup to use GitHub for authentication. It's also possible to customize the auth for your own setup (e.g. ldap, saml, etc.). Follow the steps below to use the built-in config. 

#### GitHub OAuth App

1. Visit one of these pages to create the OAuth app:
    - Personal account https://github.com/settings/developers
    - Organization: https://github.com/organizations/ORG/settings/applications (replace ORG with your org name)
2. Click on **New OAuth App**.
    - Add Application Name: This will be displayed when users login for the first time.
    - Add Homepage URL: This is your Jenkins server URL.
    - Add Authorization callback URL: `HOMEPAGE-URL/securityRealm/finishLogin` (replace HOMEPAGE-URL with your Homepage URL)
3. Click **Register Application**
4. Note the client ID and secret, this will be used in the following steps. 

## Option 1: Self-hosted

This can be used to host the docker container on your own server or run a local instance for testing.

Requirements:
- Install docker on your target platform: https://docs.docker.com/get-docker/
- Update `configs/jenkins.yaml` and provide the require values for the GitHub client ID and secret. Also add additional config files if required. See **Jenkins Configuration** section above.
> :warning: Do not commit secrets to the repo. Edit the file locally for testing or store secrets in environment variables. Use `-e key=value` with docker run to pass the env vars to the container. For other options see: https://github.com/jenkinsci/configuration-as-code-plugin/blob/master/docs/features/secrets.adoc 

Docker Container Setup:
1. Clone the repo and `cd` into the repo root
2. Build the docker image: `docker build -t jenkins .`
3. Run the docker image: `docker run -d -p 8080:8080 jenkins`

After the Jenkins service starts up, go to `http://HOST:8080`

## Option 2: Deploy to the cloud

### AWS

CDK Pipelines is used to automatically build the docker image and deploy a stack to host the container in ECS.

The pipeline has the following stages:
- Source: Triggers the pipeline when changes are merged into the target branch.
- UpdatePipeline: Allows the pipeline to self-update.
- Assets: Creates the docker image and stores it in an Elastic Container Registry (ECR) repo for the deploy stages.
- Staging: Deploys the staging instance if a staging certificate is provided.
- Prod: Deploys the prod instance. 

#### Stacks

| Stack | Path | Description |
| ------- | --- | ------ |
| Pipeline | cdk | Automates the deployment of the Jenkins server. This stack only needs to be bootstrapped and deployed once by the user. All further deployments and updates to the pipeline will be automated. |
| Jenkins Server | cdk/jenkins_server | This stack is configured to host the Jenkins service in ECS/Fargate. This can be deployed through the pipeline or through a manual stand-alone deployment. | 

#### Requirements:

> :information_source: CDK packages are written in python and the instructions below utilize `python3` and `venv`.

- AWS CDK prerequisites: https://docs.aws.amazon.com/cdk/v2/guide/work-with.html#work-with-prerequisites
    - Setup AWS credentials
    - Install Node.js and the AWS CDK Toolkit
- Required context values (passed via `cdk.context.json` or `cdk deploy --context`)
    - repo: org/repo name of the Github repo (e.g. o3de/o3de-jenkins-pipeline)
    - branch: name of the branch used for pipeline deployments
    - cert-arn: This can be an SSL cert created by or imported into AWS Certificate Manager (ACM). See **ACM SSL Certificate Setup** below
    - codestar-connection: This grants access to CodePipeline to access the GitHub repo hosting your config files. See **CodeStar Connection Setup** below.
- Optional context values
    - vpc-id: Provide an ID to use an existing VPC for the Jenkins Server stack instead of creating a new one.

##### ACM SSL Certificate Setup

Follow one of the steps below to request or import a cert. ACM will be used to associate the cert with the Jenkins service. 
- Request an ACM cert: https://docs.aws.amazon.com/acm/latest/userguide/gs-acm-request-public.html
- Import a cert: https://docs.aws.amazon.com/acm/latest/userguide/import-certificate-api-cli.html 

##### CodeStar Connection Setup

This allows AWS services to connect to third-party repos.
- Create a connection to GitHub: https://docs.aws.amazon.com/dtconsole/latest/userguide/connections-create-github.html

#### Deployment Steps:

1. Clone the repo then navigate to the CDK directory. Perform a one-time bootstrap step for each AWS account/region you'll deploy this pipeline.
    - Use` --profile` to use a named config
```
cd cdk
aws cloudformation create-stack --stack-name CDKToolkit --template-body file://bootstrap-template.yaml --capabilities CAPABILITY_NAMED_IAM
```

> :warning: The bootstrap template contains IAM policies that will be assumed by CloudFormation which are required to deploy the CDK stacks. If you have already bootstrapped this account/region for existing CDK stacks, you'll need to merge this template then run `aws cloudformation update-stack`. The policies can be found under the `CloudFormationExecutionRole:` resource. You can also skip this step if your existing CloudFormation execution policy already includes the permissions defined here or if you have granted it full permissions. 

2. Create a target branch for deployments
3. Add your configs
    - Update `cdk/cdk.context.json` with the required context values (alternatively you can provide these values using `--context` when running `cdk deploy`)
    - Update `configs/jenkins.yaml` and provide the require values for the GitHub client ID and secret. Also add additional config files if required. See **Jenkins Configuration** section above.
    > :warning: Do not commit secrets to the repo. Store secrets in AWS Parameter Store and enter the parameter names instead. For other options see: https://github.com/jenkinsci/configuration-as-code-plugin/blob/master/docs/features/secrets.adoc 
4. Push these changes to your repo/branch used for deployments
5. Deploy: This is a one-time manual deployment. Further updates will be deployed through the CDK pipeline. Follow the steps in the **CDK Deployment Steps** section below.

#### CDK Deployment Steps
Run these steps within the cdk directoy.
1. Recommended: Create a virtualenv

```
python3 -m venv .venv
```
2. Activate the virtualenv

- Mac/Linux:
```
source .venv/bin/activate
```
- Windows:
```
.venv\Scripts\activate.bat
```

3. Install the required dependenciies
```
pip install -r requirements.txt
```

4. Run cdk deploy and confirm the deployment when prompted
    - Use `--profile` to use a named config
    - Use `--context` to provide values not added to `cdk.context.json`

> :warning: Review the items listed under `IAM Statement Changes` prior to confirming and deploying the CDK stack. During the bootstrap step CloudFormation was granted IAM permissions required to create and assign roles for deployments. Pay attention to this section especially if you've customized the IAM permissions in the stack. 

```
cdk deploy
```

#### Deploying Updates

Further updates to the CDK Pipeline or the Jenkins config will be made by merging in commits to the target branch configured earlier. 

> **Recommended:** Set branch protection on your target branch to require pull request approvals before deploying changes. 
