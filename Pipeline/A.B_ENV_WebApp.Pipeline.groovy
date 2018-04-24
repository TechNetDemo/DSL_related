pipeline {
    agent {label "master"}
    stages{
        stage('Download'){
            steps{
                script{
                    try{
                        def job1 = build job: '/A.B/JOB/WebApp.DownloadArtifact', parameters: [string(name: 'artifact_version', value: artifact_version)]
                        job1_num = job1.getNumber()
                        job1_result = job1.getResult()
                    }
                    catch (err) {
                        if(currentBuild.result == null) {
                            currentBuild.result = "FAILURE" // sets the ordinal as 4 and boolean to false
                        }
                        throw err
                    }
                }
            }
        }
        stage('Replace'){
            steps{
                script{
                    try{
                        def job2 = build job: '/A.B/JOB/WebApp.ReplaceToken', parameters: [string(name: 'db_username', value: db_username), string(name: 'db_password', value: db_password), string(name: 'artifact_version', value: artifact_version)]
                        job2_num = job2.getNumber()
                        job2_result = job2.getResult()
  
                    }
                    catch (err) {
                        if(currentBuild.result == null) {
                            currentBuild.result = "FAILURE" // sets the ordinal as 4 and boolean to false
                        }
                        throw err
                    }
                }
            }
        }
        stage('Deploy'){
            steps{
                script{
                    try{
                         def job3 = build job: '/A.B/JOB/WebApp.Deploy', parameters: [string(name: 'artifact_version', value: artifact_version), [$class: 'LabelParameterValue', name: 'node_to_run', label: node_to_run, allNodesMatchingLabel: true, nodeEligibility: [$class: 'AllNodeEligibility']]]
                         job3_num = job3.getNumber()
                         job3_result = job3.getResult()
                         
                    if(currentBuild.result == null) {
                            currentBuild.result = "SUCCESS" // sets the ordinal as 0 and boolean to true
                        }
  
                    }
                    catch (err) {
                        if(currentBuild.result == null) {
                            currentBuild.result = "FAILURE" // sets the ordinal as 4 and boolean to false
                        }
                        throw err
                    }
                }
            }
        }

    }
    post {
            success{
                script{
                    def mydata = [:]
                    mydata['pipeline'] = upstream
                    mydata['job1_BuildNum'] = job1_num
                    mydata['job1_result'] = job1_result
                    mydata['job2_BuildNum'] = job2_num
                    mydata['job2_result'] = job2_result
                    mydata['job3_BuildNum'] = job3_num
                    mydata['job3_result'] = job3_result
                    step([$class: 'InfluxDbPublisher',
                        customData: mydata,
                        customDataMap: null,
                        target: 'DSL_DB'])
                }
            }
            aborted {
                echo '<aborted message>'
                script{
                    def mydata = [:]
                    mydata['pipeline'] = upstream
                    mydata['job1_BuildNum'] = job1_num
                    mydata['job1_result'] = job1_result
                    mydata['job2_BuildNum'] = job2_num
                    mydata['job2_result'] = job2_result
                    mydata['job3_BuildNum'] = job3_num
                    mydata['job3_result'] = job3_result
                    step([$class: 'InfluxDbPublisher',
                        customData: mydata,
                        customDataMap: null,
                        target: 'DSL_DB'])
                }
            }
            failure {
                echo '<rollback process>'
                script{
                    currentBuild.result = "ABORTED"
                    def mydata = [:]
                    mydata['pipeline'] = upstream
                    mydata['job1_BuildNum'] = job1_num
                    mydata['job1_result'] = job1_result
                    mydata['job2_BuildNum'] = job2_num
                    mydata['job2_result'] = job2_result
                    mydata['job3_BuildNum'] = job3_num
                    mydata['job3_result'] = job3_result
                    step([$class: 'InfluxDbPublisher',
                        customData: mydata,
                        customDataMap: null,
                        target: 'DSL_DB'])
                }
            }
    }

}