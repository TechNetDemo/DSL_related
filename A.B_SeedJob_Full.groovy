folder('A.B')

folder('A.B/JOB')
folder('A.B/DEV')
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
    stringParam('db_password','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_url','dbc:mysql://mydbinstance.c3aqksy4y3yi.us-east-1.rds.amazonaws.com:3306/WebAppDB','description written in DSL script')
    stringParam('db_tableName','film','description written in DSL script')
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
       allNodes('allCases','AllNodeEligibility')
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
  }
}
//A.B_DEV_Pipeline
pipelineJob('A.B/DEV/A.B_DEV_Pipeline'){
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_password','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
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
//A.B_DEV_WebApp.Pipeline
pipelineJob('A.B/DEV/A.B_DEV_WebApp.Pipeline'){
  parameters {
    stringParam('artifact_version','From_TOP_Pipeline','Input at TOP_Pipeline')
    stringParam('db_username','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    stringParam('db_password','From_TOP_Pipeline','Runtime input TOP_Pipeline-<ENV> stage')
    labelParam('node_to_run'){
      description('Specify the agents to deploy')
      defaultValue('dev_label')
      allNodes('allCases','AllNodeEligibility')
    }
  }
  definition {
    cpsScm{
      scm{
        git('https://github.com/TechNetDemo/DSL_related.git')
      }
      scriptPath('Pipeline/A.B_DEV_WebApp.Pipeline.groovy')
    }
  }  
}
