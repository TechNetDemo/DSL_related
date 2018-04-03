folder('DSLteam1/SIT')
job('DSLteam1/SIT/job1'){
  description 'This is the first step of Team1-SIT'
  customWorkspace('C:\\Jenkins_agent\\workspace\\DSLteam1\\SIT\\pipeline1\\ArtifactDirectory\\com\\technet\\webapp-team1\\${artifact_version}')
  parameters {
    stringParam('artifact_version','0.1.0-SNAPSHOT','description written in DSL script')
    stringParam('db_username','root','description written in DSL script')
    stringParam('db_password','0000abc!','description written in DSL script')
    stringParam('db_url','dbc:mysql://mydbinstance.c3aqksy4y3yi.us-east-1.rds.amazonaws.com:3306/WebAppDB','description written in DSL script')
    stringParam('db_tableName','film','description written in DSL script')
    labelParam('node_to_run'){
      description('nodelabel specified in DSL')
      defaultValue('sit_label')
      allNodes('allCases','AllNodeEligibility')
    }
  }  
  steps{
    batchFile('jar -xvf *.war')
    systemGroovyCommand(readFileFromWorkspace('replaceToken.groovy'))
    batchFile('move *.war ..\\\njar -cvf webApp.war *')
  }
  configure{ project ->
    project / publishers << 'jenkinsci.plugins.influxdb.InfluxDbPublisher' {
      
        selectedTarget('InfluxDB for build information')  
    }
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
pipelineJob('DSLteam1/SIT/pipeline1'){
  parameters {
    stringParam('artifact_version','0.1.0-SNAPSHOT','description written in DSL script')
    stringParam('db_username','root','description written in DSL script')
    stringParam('db_password','0000abc!','description written in DSL script')
    stringParam('db_url','dbc:mysql://mydbinstance.c3aqksy4y3yi.us-east-1.rds.amazonaws.com:3306/<specify SIT DB here>','description written in DSL script')
    stringParam('db_tableName','film','description written in DSL script')
    labelParam('node_to_run'){
      description('nodelabel specified in DSL')
      defaultValue('sit_label')
      allNodes('allCases','AllNodeEligibility')
    }
  }
  definition {
    cps{
      script(readFileFromWorkspace('deployWebAppPipeline.groovy'))
       }
  }  
}