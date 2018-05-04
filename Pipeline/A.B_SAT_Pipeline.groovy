pipeline {
    agent {label "master"}
    stages{
        stage('Approval'){
            steps{
                input message: 'Start SAT Deployment?', submitter: 'sat_admin'
            }
        }
        stage('WebApp'){
            steps{
                build job: '/A.B/SAT/A.B_SAT_WebApp.Pipeline', parameters: [string(name: 'envir', value: 'SAT'), string(name: 'upstream', value: JOB_NAME),string(name: 'artifact_version', value: artifact_version),string(name: 'db_username', value: db_username),string(name: 'db_password', value: db_password)]
            }
        }
        stage('Component2'){
            steps{
                echo 'Run another component pipeline <A.B_SAT_component2.Pipeline>'
            }
        }
    }
    post {
            success{
                script{
                    pipelineData =[:]
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = "SUCCESS"
                    currentBuild.result = "SUCCESS"
                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'PIPELINES_DB'])
                }
            }
            aborted {
                echo '<aborted message>'
                script{
                    pipelineData =[:]
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = "ABORTED"
                    currentBuild.result = "ABORTED"
                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'PIPELINES_DB'])
                }
            }
            failure {
                echo '<rollback process>'
                script{
                    pipelineData =[:]
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = "FAILURE"
                    currentBuild.result = "FAILURE"
                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'PIPELINES_DB'])
                }
            }
    }
}