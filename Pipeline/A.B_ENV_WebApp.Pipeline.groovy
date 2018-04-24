pipeline {
    agent {label "master"}
    stages{
        stage('Download'){
            steps{
                script{
                        job1 = build job: '/A.B/JOB/WebApp.DownloadArtifact', parameters: [string(name: 'artifact_version', value: artifact_version)]
                        mydata = [:]
                        mydata['upstream'] = upstream
                        mydata['job1_BuildNum'] = job1.getNumber()
                        mydata['job1_Result'] = job1.getResult()

                }
            }
        }
        stage('Replace'){
            steps{
                script{

                        job2 = build job: '/A.B/JOB/WebApp.ReplaceToken', parameters: [string(name: 'db_username', value: db_username), string(name: 'db_password', value: db_password), string(name: 'artifact_version', value: artifact_version)]
                        mydata['job2_BuildNum'] = job2.getNumber()
                        mydata['job2_Result'] = job2.getResult()

                }
            }
        }
        stage('Deploy'){
            steps{
                script{

                        job3 = build job: '/A.B/JOB/WebApp.Deploy', parameters: [string(name: 'artifact_version', value: artifact_version), [$class: 'LabelParameterValue', name: 'node_to_run', label: node_to_run, allNodesMatchingLabel: true, nodeEligibility: [$class: 'AllNodeEligibility']]]
                        mydata['job3_BuildNum'] = job3.getNumber()
                        mydata['job3_Result'] = job3.getResult()
                         

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