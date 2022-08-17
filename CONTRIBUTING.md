# Contributing

To contribute, please review our [Code of Conduct](https://www.o3de.org/docs/contributing/code-of-conduct/) first. 

## Getting Started

Requirements:

- [Docker Desktop](https://docs.docker.com/get-docker/): Required to test changes made to the Jenkins server configs on your machine

Optional Resources:

- [AWS CDK (Python)](https://docs.aws.amazon.com/cdk/v2/guide/work-with-cdk-python.html): Only required if you need to test changes made to the CDK stacks

## Jenkins Container Setup

This section contains details on how the Jenkins docker container is configured and the steps that occur at startup. This setup is different from how Jenkins runs on a typical server install. 

### JENKINS_HOME

One important note about JENKINS_HOME is that it is captured as a [Volume](https://docs.docker.com/storage/volumes/) in the parent image. This prevents us from changing the contents of the directory when creating downstream images ([More info](https://docs.docker.com/engine/reference/builder/#notes-about-specifying-volumes)).

In order to add contents to JENKINS_HOME, files must be copied into the $REF directory (set to /usr/share/jenkins/ref). The contents of this directory will be copied into JENKINS_HOME when the container starts.

### Entrypoint

To run Jenkins, the parent image is configured to using the following entrypoint.

```
ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/jenkins.sh"]
```

### Custom Entrypoint

We utilize a custom entrypoint to clear out the contents of the plugins directory prior to startup. The default location of this directory is in $JENKINS_HOME which is stored on a shared filesystem and there currently isn't an option to change this location. This results in stale plugin data that needs to be deleted or manually uninstalled from the Jenkins UI. 

The custom entrypoint is configured to perform our required tasks then call the original entrypoint listed above. 

### Jenkins Startup

After the start up scripts are executed and Jenkins starts, it also performs the following actions:

- Loads the plugins installed into $JENKINS\_HOME/plugins
- Uses the [JCasC](https://www.jenkins.io/projects/jcasc/) plugin to load the config files and job DSL scripts. The CASC_JENKINS_CONFIG environment variable is used to tell the plugin where to find the files. 
    - The env var is set to ${JENKINS_LOCAL}/configs in our Dockerfile. 
    - This is moved off the default location of $JENKINS_HOME to avoid stale configs when the config files are updated.

## Making contributions with the Developer Certificate of Origin (DCO)

When contributing, your pull requests will require that you have agreed to our DCO found here: [Developer Certificate of Origin](https://developercertificate.org/).  All commits require the --signoff flag to show DCO compliance.

You can do this by using the -s option in git. 
Example: ```git commit -s -m 'my commit message'```

## Getting Help

If you have any questions or issues about the O3DE Jenkins Pipeline setup, reach out to the Build SIG by using the resources below:

*   Discord Channel: [#sig-build](https://discord.com/channels/805939474655346758/816043576034328636)
*   GitHub Issues: [Create Issue](https://github.com/o3de/o3de-jenkins-pipeline/issues/new)
