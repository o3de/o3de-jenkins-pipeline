# o3de-jenkins-pipeline


## Jenkins Configuration

The O3DE Jenkins pipeline is configured using the JCasC plugin. This allows the settings to be defined using yaml files stored in the repo and prevents users from having to manually configure Jenkins on startup. 

These files are located in the configs/ directory. There is a main jenkins.yaml file that contains the recommended default settings to run the pipeline. You can customize your Jenkins setup by editing this file. There are also template files that can be used for other optional settings. You can either copy the contents of these files into jenkins.yaml or copy it into a new yaml file. 


### Config File location
The config files must be located in the configs/ directory so that they are found by the plugin. The plugin also supports having multiple yaml files and will locate all files having the .yml/.yaml extention.
