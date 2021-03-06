node('master') {
    try{
    def server = Artifactory.server ('AWS-Artifactory')
    def downloadSpec = """{
        "files": [
            {
              "pattern": "libs-snapshot-local/com/technet/webapp-team1/${artifact_version}/*.war",
              "target": "ArtifactDirectory/"
            }
        ]
    }"""
        server.download(downloadSpec);
        if(currentBuild.result == null) {
            currentBuild.result = "SUCCESS" // sets the ordinal as 0 and boolean to true
                step([$class: 'InfluxDbPublisher',
                customData: null,
                customDataMap: null,
                target: 'JOBS_DB'])
        }
  
    }
        catch (err) {
            if(currentBuild.result == null) {
                currentBuild.result = "FAILURE" // sets the ordinal as 4 and boolean to false
                step([$class: 'InfluxDbPublisher',
                customData: null,
                customDataMap: null,
                target: 'JOBS_DB'])
            }
            throw err
        }

}