#
# Copyright (c) Contributors to the Open 3D Engine Project
#
# SPDX-License-Identifier: Apache-2.0 OR MIT
#

from aws_cdk import (
    aws_codebuild as codebuild,
    aws_codecommit as codecommit,
    aws_codedeploy as codedeploy,
    aws_codepipeline as codepipeline,
    aws_codepipeline_actions as codepipeline_actions,
    aws_iam as iam, 
    core
)


class JenkinsPipeline(core.Stack):
    """Defines the pipeline to deploy a Jenkins server.
    
    Source stage: Pulls dockerfile and buildspec.yaml from the repo to build the docker image.
    Build stage: Builds a docker image then uploads it to ECR.
    Deploy stage: Uses CodeDeploy to start a new ECS task and replace the existing one.

    Attributes:
        codedeploy_app_name: Name of the target CodeDeploy Application for the pipeline
        codedeploy_group: Name of the target Deployment Group for the pipeline
        repo_name: Name of the repo that stores the dockerfile and pipeline config files
        repo_branch: Name of the branch used as the source of the pipeline

    """

    def __init__(self, scope: core.Construct, id: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        self.codedeploy_app_name = core.CfnParameter(self, "CodeDeployAppName", type="String").value_as_string
        self.codedeploy_group_name = core.CfnParameter(self, "CodeDeployGroupName", type="String").value_as_string
        self.repo_name = core.CfnParameter(self, "RepoName", type="String").value_as_string
        self.repo_branch = core.CfnParameter(self, "RepoBranch", type="String").value_as_string

        self._create_pipeline_stack()

    def _create_pipeline_stack(self):
        repo = self._import_repo()
        codedeploy_group = self._import_codedeploy_group()

        codebuild_role = self._create_role()
        codebuild_project = self._create_codebuild_project(codebuild_role)

        self._create_pipeline(repo, codebuild_project, codedeploy_group)

    def _import_repo(self):
        return codecommit.Repository.from_repository_name(self, "DockerImageRepo", self.repo_name)

    def _import_codedeploy_group(self):
        codedeploy_app = codedeploy.EcsApplication.from_ecs_application_name(self, "CodeDeployApp", self.codedeploy_app_name)
        return codedeploy.EcsDeploymentGroup.from_ecs_deployment_group_attributes(self, "CodeDeployGroup", 
                                                                                    application=codedeploy_app, 
                                                                                    deployment_group_name=self.codedeploy_group_name)

    def _create_role(self):
        # TODO: Find another location to store IAM role configs
        codebuild_managed_policies = [
            "AmazonEC2ContainerRegistryPowerUser",
            "AmazonSSMReadOnlyAccess"
        ]

        codebuild_actions = [
            "logs:CreateLogGroup",
            "logs:CreateLogStream",
            "logs:PutLogEvents",
            "codecommit:GitPull",
            "s3:GetObject",
            "s3:GetObjectVersion",
            "s3:GetBucketAcl",
            "s3:GetBucketLocation",
            "s3:PutObject",
            "ecr:BatchCheckLayerAvailability",
            "ecr:GetDownloadUrlForLayer",
            "ecr:BatchGetImage",
            "ecr:GetAuthorizationToken",
            "codebuild:CreateReportGroup",
            "codebuild:CreateReport",
            "codebuild:UpdateReport",
            "codebuild:BatchPutTestCases"
        ]

        codebuild_role = iam.Role(self, "CodeBuildRole", assumed_by=iam.ServicePrincipal("codebuild.amazonaws.com"))

        for policy in codebuild_managed_policies:
            codebuild_role.add_managed_policy(iam.ManagedPolicy.from_aws_managed_policy_name(policy))
        
        codebuild_role.add_to_policy(iam.PolicyStatement(actions=codebuild_actions, resources=["*"]))

        return codebuild_role
    
    def _create_codebuild_project(self, codebuild_role):
        return codebuild.PipelineProject(self, "DockerImageBuild",
                                            # Privileged env required to create docker images
                                            environment=codebuild.BuildEnvironment(privileged=True),
                                            role=codebuild_role
                                        )
    
    def _create_pipeline(self, repo, codebuild_project, codedeploy_group):
        source_output = codepipeline.Artifact("SourceOutput")
        image_build_output = codepipeline.Artifact("CdkBuildOutput")

        codepipeline.Pipeline(self, "JenkinsServerPipeline",
            stages=[
                codepipeline.StageProps(stage_name="Source",
                    actions=[
                        codepipeline_actions.CodeCommitSourceAction(
                            action_name="CodeCommitSource",
                            repository=repo,
                            branch=self.repo_branch,
                            output=source_output)]),
                codepipeline.StageProps(stage_name="Build",
                    actions=[
                        codepipeline_actions.CodeBuildAction(
                            action_name="BuildDockerImage",
                            project=codebuild_project,
                            input=source_output,
                            outputs=[image_build_output])]),
                codepipeline.StageProps(stage_name="Deploy",
                    actions=[
                        codepipeline_actions.CodeDeployEcsDeployAction(
                            action_name="DeployJenkinsServer",
                            app_spec_template_input=source_output,
                            deployment_group=codedeploy_group,
                            task_definition_template_input=source_output)])
            ]
        )
