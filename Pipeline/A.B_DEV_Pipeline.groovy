pipeline {
    agent {label "master"}
    stages{
		stage('Approval'){
			steps{
                input message: 'Start DEV Deployment?', submitter: 'dev_admin'
            }
        }
        stage('WebApp'){
            steps{
                build job: '/A.B/DEV/A.B_DEV_WebApp.Pipeline', parameters: [string(name: 'envir', value: 'DEV'), string(name: 'upstream', value: JOB_NAME),string(name: 'artifact_version', value: artifact_version),string(name: 'db_username', value: db_username),string(name: 'db_password', value: db_password)]
            }
        }
        stage('Component2'){
            steps{
                echo 'Run another component pipeline <A.B_DEV_component2.Pipeline>'
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