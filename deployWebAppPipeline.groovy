node(node_to_run) {
    stage('download'){
    try{
    def server = Artifactory.server 'AWS-Artifactory'
    def downloadSpec = """{
        "files": [
            {
              "pattern": "libs-snapshot-local/com/technet/webapp-team1/${artifact_version}/*.war",
              "target": "ArtifactDirectory/"
            }
        ]
    }"""
        server.download(downloadSpec);
        //build job: '/DSLteam1/DEV/job1', parameters: [string(name: 'db_username', value: db_username), string(name: 'db_password', value: db_password), string(name: 'db_url', value: db_url), string(name: 'db_tableName', value: db_tableName), string(name: 'artifact_version', value: artifact_version),[$class: 'NodeParameterValue', name: 'node_to_run', allNodesMatchingLabel: true,labels: [node_to_run], nodeEligibility: [$class: 'AllNodeEligibility'], triggerIfResult: 'allCases']]
        
        if(currentBuild.result == null) {
            currentBuild.result = "SUCCESS" // sets the ordinal as 4 and boolean to false
        }
    }
    catch (err) {
        if(currentBuild.result == null) {
            currentBuild.result = "FAILURE" // sets the ordinal as 4 and boolean to false
        }
         throw err
    }
    }
    stage('post'){
                def mydata = [:]
                mydata['pipeline'] = upstream
                
                step([$class: 'InfluxDbPublisher',
                    customData: mydata,
                    customDataMap: null,
                    target: 'DSL_DB'])
    }
}



