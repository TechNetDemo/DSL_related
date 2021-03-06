//10/5 Combined WebApp.Pipeline  to /JOB, db_password masked
folder('A.B')

folder('A.B/JOB')
folder('A.B/BUILD')
folder('A.B/DEV')
folder('A.B/SIT')
folder('A.B/SAT')
folder('A.B/PROD')
//WebApp.DownloadArtifact
pipelineJob('A.B/JOB/WebApp.DownloadArtifact'){
  description 'WebApp Step 1'
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
  }  
  definition {
     cps{
      script(readFileFromWorkspace('JOB/WebApp.DownloadArtifact.groovy'))
     }
  }
}
//WebApp.ReplaceToken
job('A.B/JOB/WebApp.ReplaceToken'){
  label('master')
  description 'A.B/JOB/WebApp.ReplaceToken'
  customWorkspace('/var/lib/jenkins/workspace/A.B/JOB/WebApp.DownloadArtifact/ArtifactDirectory/com/technet/webapp-team1/${artifact_version}')
  configure { project ->
    project / publishers << 'jenkinsci.plugins.influxdb.InfluxDbPublisher' {      
        selectedTarget('JOBS_DB')
    }
    
    project / 'buildWrappers' / 'hudson.plugins.ws__cleanup.PreBuildCleanup' {
        patterns{
            'hudson.plugins.ws__cleanup.Pattern'{
              pattern('**/*-1.war')
              type('EXCLUDE')
           }
        }
        deleteDirs(true)
    }
  }
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    nonStoredPasswordParam('db_password','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_tableName','film','description written in DSL script')
    stringParam('db_url','dbc:mysql://mydbinstance.c3aqksy4y3yi.us-east-1.rds.amazonaws.com:3306/WebAppDB','description written in DSL script')
  }  
  steps{
    shell('jar -xvf *.war')
    systemGroovyCommand(readFileFromWorkspace('JOB/WebApp.ReplaceToken.groovy'))
    shell('jar -cvf webApp.war *')
  }
  publishers {
    archiveArtifacts('webApp.war')
  }  
}
//WebApp.Deploy
job('A.B/JOB/WebApp.Deploy'){
  concurrentBuild()
   parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
     labelParam('node_to_run'){
       description('Input at ENV_Pipeline')
       defaultValue('test_label')
     }
   }
  steps{
    copyArtifacts('/A.B/JOB/WebApp.ReplaceToken'){
      includePatterns('webApp.war')
    } 
  }
  configure{ project ->
    project / publishers << 'hudson.plugins.deploy.DeployPublisher' {
      adapters{
        'hudson.plugins.deploy.tomcat.Tomcat7xAdapter'{
          credentialsId('Jenkins_deployer')
          url('http://localhost:8080')
        }
      }
      war('webApp.war')
      onFailure(false)
    }
    
    project / publishers << 'jenkinsci.plugins.influxdb.InfluxDbPublisher' {      
        selectedTarget('JOBS_DB')
    }
  }
}

//A.B/JOB/A.B_WebApp.Pipeline
pipelineJob('A.B/JOB/A.B_WebApp.Pipeline'){
  parameters {
    stringParam('envir','TEST','passed from ENV_Pipeline')
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    nonStoredPasswordParam('db_password','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_tableName','From_ENV_Pipeline','Input at ENV_Pipeline')
    stringParam('upstream','No_Upstream','passed from ENV_Pipeline')
    labelParam('node_to_run'){
      description('passed from ENV_Pipeline')
      defaultValue('Error: no value input')
    }
  }
  definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_ENV_WebApp.Pipeline.groovy')
    }
  }  
}
                
//A.B_Build_WebApp.Build
job('A.B/BUILD/A.B_BUILD_WebApp.Build'){
  scm{
      git{
          remote{
        github('TechNetDemo/WebAppDemo')
        credentials('4421f092-feb9-4616-96ba-2da48785e825')
          }
      }
  }
    
  configure { project ->
    project / 'builders' / 'org.jfrog.hudson.maven3.Maven3Builder' {
        mavenName('Maven')
        rootPom('webapp-team1/pom.xml')
        goals('clean install')
        
    }
  }  
    
  configure { project ->
    project / 'buildWrappers' / 'org.jfrog.hudson.maven3.ArtifactoryMaven3Configurator' {
      deployArtifacts(true)
      deployBuildInfo(true)
      details{
            artifactoryName('AWS-Artifactory')
            artifactoryUrl('http://ip-172-31-92-116.ec2.internal:8081/artifactory')
            deployReleaseRepository{
                keyFromSelect('libs-release-local')
            }
            deploySnapshotRepository{
                keyFromSelect('libs-snapshot-local')
          }
        }
        resolverDetails{
            artifactoryName('AWS-Artifactory')
            artifactoryUrl('http://ip-172-31-92-116.ec2.internal:8081/artifactory')
            resolveReleaseRepository{
                keyFromSelect('libs-release-local')
            }
            resolveSnapshotRepository{
                keyFromSelect('libs-snapshot-local')
          }
        }
    }
  }    
}

//A.B_DEV_Pipeline
pipelineJob('A.B/DEV/A.B_DEV_Pipeline'){
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    nonStoredPasswordParam('db_password','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_tableName','film10','options: film, film10, film20')
    stringParam('upstream','No_Upstream','passed from TOP_Pipeline')
    stringParam('node_to_run','dev_label','Specify the agents to deploy (for WebApp component)')
  }
  definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_DEV_Pipeline.groovy')
    }
  }  
}

//A.B_SIT_Pipeline
pipelineJob('A.B/SIT/A.B_SIT_Pipeline'){
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    nonStoredPasswordParam('db_password','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_tableName','film','options: film, film10, film20')
    stringParam('upstream','No_Upstream','passed from TOP_Pipeline')
    stringParam('node_to_run','sit_label','Specify the agents to deploy (for WebApp component)')
  }
  definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_SIT_Pipeline.groovy')
    }
  }  
}

//A.B_SAT_Pipeline
pipelineJob('A.B/SAT/A.B_SAT_Pipeline'){
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    nonStoredPasswordParam('db_password','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_tableName','film','options: film, film10, film20')
    stringParam('upstream','No_Upstream','passed from TOP_Pipeline')
    stringParam('node_to_run','sat_label','Specify the agents to deploy (for WebApp component)')
  }
  definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_SAT_Pipeline.groovy')
    }
  }  
}

//A.B_PROD_Pipeline
pipelineJob('A.B/PROD/A.B_PROD_Pipeline'){
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    nonStoredPasswordParam('db_password','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_tableName','film','options: film, film10, film20')
    stringParam('upstream','No_Upstream','passed from TOP_Pipeline')
    stringParam('node_to_run','prod_label','Specify the agents to deploy (for WebApp component)')
  }
  definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_PROD_Pipeline.groovy')
    }
  }  
}

//A.B_TOP_Pipeline
pipelineJob('A.B/A.B_TOP_Pipeline'){
   parameters {
    stringParam('artifact_version','0.1.0-SNAPSHOT','Artifact version used for deployment')
   }
   definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_TOP_Pipeline.groovy')
    }
  }  
}