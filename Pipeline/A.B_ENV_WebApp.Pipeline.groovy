pipeline {
    agent {label "master"}
    stages{
        stage('Download'){
            steps{
                script{
                    try{
                        build job: '/A.B/JOB/WebApp.DownloadArtifact', parameters: [string(name: 'artifact_version', value: artifact_version)]
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
        stage('Replace'){
            steps{
                script{
                    try{
                        build job: '/A.B/JOB/WebApp.ReplaceToken', parameters: [string(name: 'db_username', value: db_username), string(name: 'db_password', value: db_password), string(name: 'artifact_version', value: artifact_version)]
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
        stage('Deploy'){
            steps{
                script{
                    try{
                         build job: '/A.B/JOB/WebApp.Deploy', parameters: [string(name: 'artifact_version', value: artifact_version), [$class: 'LabelParameterValue', name: 'node_to_run', label: node_to_run, allNodesMatchingLabel: true, nodeEligibility: [$class: 'AllNodeEligibility']]]
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
                step([$class: 'InfluxDbPublisher',
                    customData: null,
                    customDataMap: null,
                    target: 'pipelineTestDB'])
            }
            aborted {
                echo '<aborted message>'
                script{
                    currentBuild.result = "ABORTED"
                    step([$class: 'InfluxDbPublisher',
                        customData: null,
                        customDataMap: null,
                        target: 'pipelineTestDB'])
                }
            }
            failure {
                echo '<rollback process>'
                script{
                    step([$class: 'InfluxDbPublisher',
                        customData: null,
                        customDataMap: null,
                        target: 'pipelineTestDB'])
                }
            }
    }

}