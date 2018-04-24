pipeline {
    agent {label "master"}
    stages{
        stage('WebApp'){
            steps{
                script{
                        mydata =[:]
                        mydata['upstream'] = upstream
                        
                        Job_WebApp= build job: '/A.B/DEV/WebApp.DataCollect.Pipeline', parameters: [string(name: 'upstream', value: JOB_NAME),string(name: 'artifact_version', value: artifact_version),string(name: 'db_username', value: db_username),string(name: 'db_password', value: db_password)]
                        
                        mydata['job1_BuildNum'] = Job_WebApp.getNumber()
                        mydata['job1_Result'] = Job_WebApp.getResult()

                }
            }
        }
        stage('Component2'){
            steps{
                script{
                        echo 'Run another component pipeline <A.B_DEV_component2.Pipeline>'
                }
            }
        }
    }
    post {
            success{
                script{
                    currentBuild.result = "SUCCESS"
                    step([$class: 'InfluxDbPublisher',
                        customData: mydata,
                        customDataMap: null,
                        target: 'DSL_DB'])
                }
            }
            aborted {
                echo '<aborted message>'
                script{
                    currentBuild.result = "ABORTED"
                    step([$class: 'InfluxDbPublisher',
                        customData: mydata,
                        customDataMap: null,
                        target: 'DSL_DB'])
                }
            }
            failure {
                echo '<rollback process>'
                script{
                    currentBuild.result = "FAILURE"
                    step([$class: 'InfluxDbPublisher',
                        customData: mydata,
                        customDataMap: null,
                        target: 'DSL_DB'])
                }
            }
    }
}