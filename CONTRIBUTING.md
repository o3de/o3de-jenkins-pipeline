# Contributing

To contribute, please review our [Code of Conduct](https://www.o3de.org/docs/contributing/code-of-conduct/) first. 

## Getting Started

This section contains details on how the Jenkins container starts up using the scripts defined in the parent docker image. This startup process is different from how it runs on a typical server install. 

### Jenkins Parent Image

This image is maintained by the Jenkins community and published to Docker hub. This is the image we use when creating our own custom docker container. 

-  GitHub repo: [https://github.com/jenkinsci/docker](https://github.com/jenkinsci/docker)
- Docker Hub: [https://hub.docker.com/r/jenkins/jenkins](https://hub.docker.com/r/jenkins/jenkins)

#### JENKINS_HOME

One important note about JENKINS_HOME is that it is captured as a [Volume](https://docs.docker.com/storage/volumes/) in the parent image. This prevents us from changing the contents of the directory when creating downstream images ([More info](https://docs.docker.com/engine/reference/builder/#notes-about-specifying-volumes)).

In order to add contents to JENKINS_HOME, files must be copied into the $REF directory (set to /usr/share/jenkins/ref). The contents of this directory will be copied into JENKINS_HOME when the container starts.

### Entrypoint

To run Jenkins, the parent image is configured to using the following entrypoint.

```
ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/jenkins.sh"]
```

The [jenkins.sh](https://github.com/jenkinsci/docker/blob/master/jenkins.sh) script performs these main actions:

- Copies the contents of $REF to $JENKINS\_HOME by running the [jenkins-support.sh](https://github.com/jenkinsci/docker/blob/master/jenkins-support) script.
    - NOTE: If symlinks are added to $REF, the contents of the target file/directory are copied not the symlink itself. 
- Loads Java and Jenkins options ($JAVA_OPTS and $JENKINS_OPTS)
- Runs the Jenkins WAR file to start the service using the options that are provided.

### O3DE Jenkins Image

Using the parent image above, we use a dockerfile to generate our custom image. This allows us to install our required plugins and load other custom options. 

- Dockerfile: https://github.com/o3de/o3de-jenkins-pipeline/blob/main/Dockerfile

#### Custom Entrypoint

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
