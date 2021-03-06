pipeline {
    agent {label "master"}
    stages{
        stage('WebApp'){
            steps{
                build job: '/FO.CSA/JOB/FO.CSA_WebApp.Pipeline', parameters: [[$class: 'NodeParameterValue', name: 'node_to_run', allNodesMatchingLabel: true,labels: [node_to_run], nodeEligibility: [$class: 'AllNodeEligibility']],string(name: 'envir', value: 'SIT'), string(name: 'upstream', value: JOB_NAME),string(name: 'artifact_version', value: artifact_version),string(name: 'db_username', value: db_username),[$class: 'com.michelin.cio.hudson.plugins.passwordparam.PasswordParameterValue', name: 'db_password', value: db_password],string(name: 'db_tableName', value: db_tableName)]
            }
        }
        stage('Component2'){
            steps{
                echo 'Run another component pipeline <FO.CSA_SIT_component2.Pipeline>'
            }
        }
    }
    post {
            success{
                script{
                    pipelineData =[:]
                    pipelineData['environment'] = 'SIT'
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = "SUCCESS"
                    pipelineData['pipeline_ExpectedTime'] = 120
                    currentBuild.result = "SUCCESS"

                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'InfluxDBPipelines'])
                }
            }
            aborted {
                echo '<aborted message>'
                script{
                    pipelineData =[:]
                    pipelineData['environment'] = 'SIT'
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = "ABORTED"
                    pipelineData['pipeline_ExpectedTime'] = 120
                    currentBuild.result = "ABORTED"

                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'InfluxDBPipelines'])
                }
            }
            failure {
                echo '<rollback process>'
                script{
                    pipelineData =[:]
                    pipelineData['environment'] = 'SIT'
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = "FAILURE"
                    pipelineData['pipeline_ExpectedTime'] = 120
                    currentBuild.result = "FAILURE"
                    
                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'InfluxDBPipelines'])
                }
            }
    }
}