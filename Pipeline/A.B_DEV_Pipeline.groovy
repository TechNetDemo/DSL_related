pipeline {
    agent {label "master"}
    stages{
        stage('WebApp'){
            steps{
                script{
                    try{
                        
                        build job: '/A.B/DEV/A.B_DEV_WebApp.Pipeline', parameters: [string(name: 'artifact_version', value: artifact_version),string(name: 'db_username', value: db_username),string(name: 'db_password', value: db_password)]
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
        stage('Component2'){
            steps{
                script{
                    try{
                        echo 'Run another component pipeline <FO.CSA_DEV_component2.Pipeline>'
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
                    target: 'stgDB'])
            }
            aborted {
                echo '<aborted message>'
                script{
                    currentBuild.result = "ABORTED"
                    step([$class: 'InfluxDbPublisher',
                        customData: null,
                        customDataMap: null,
                        target: 'stgDB'])
                }
            }
            failure {
                echo '<rollback process>'
                script{
                    step([$class: 'InfluxDbPublisher',
                        customData: null,
                        customDataMap: null,
                        target: 'stgDB'])
                }
            }
    }
}