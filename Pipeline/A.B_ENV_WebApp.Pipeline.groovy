pipeline {
    agent {label "master"}
    stages{
        stage('Download'){
            steps{
                script{
                    myList =[]
                    job1 = build job: '/A.B/JOB/WebApp.DownloadArtifact', propagate: false, parameters: [string(name: 'artifact_version', value: artifact_version)]
                    
                    def mydata = [:]
                    currentBuild.result = job1.getResult()
                    mydata['job_JobName'] = job1.getProjectName()
                    mydata['job_BuildNumber'] = job1.getNumber()
                    mydata['job_StartTime'] = job1.getStartTimeInMillis()
                    mydata['job_Duration'] = job1.getDuration()
                    mydata['job_Result'] = job1.getResult()
                    mydata['environment'] = envir
                    mydata['job_NodeName'] = 'Master'
                    myList << mydata
                    //echo 'myList size: ' + myList.size()
                    
                    if(job1.getResult()!= 'SUCCESS'){
                        currentBuild.result = 'FAILURE'
                        error (mydata['job_JobName'] + ' Failed')
                    }
                }
            }
                    //props = readProperties file: 'PropFile/webapp_prop.properties'
                    //echo 'params in propFile: ' + prop['db_url']
            
        }
        stage('Replace'){
            steps{
                script{
                        job2 = build job: '/A.B/JOB/WebApp.ReplaceToken', propagate: false, parameters: [string(name: 'db_username', value: db_username), [$class: 'com.michelin.cio.hudson.plugins.passwordparam.PasswordParameterValue', name: 'db_password', value: db_password], string(name: 'db_tableName', value: db_tableName), string(name: 'artifact_version', value: artifact_version)]
                       
                        def mydata = [:]
                        mydata['job_JobName'] = job2.getProjectName()
                        mydata['job_BuildNumber'] = job2.getNumber()
                        mydata['job_StartTime'] = job2.getStartTimeInMillis()
                        mydata['job_Duration'] = job2.getDuration()
                        mydata['job_NodeName'] = 'Master'
                        mydata['job_Result'] = job2.getResult()
                        mydata['environment'] = envir
                        myList << mydata
                        
                        if(job2.getResult()!= 'SUCCESS'){
                            currentBuild.result = 'FAILURE'
                            error (mydata['job_JobName'] + ' Failed')
                        }
                }
            }
        }
        
        stage('Deploy'){
            steps{
                script{
                    def tasks = [:]
                    agentCount = nodesByLabel(label: node_to_run).size()
                    nodeList = nodesByLabel(label: node_to_run)

                
                    for(int j=0; j<agentCount; j++){
                        def i = j
                        tasks["Task ${nodeList[i]}"]= {
                            job3 = build job: '/A.B/JOB/WebApp.Deploy', propagate: false, parameters: [string(name: 'artifact_version', value: artifact_version), [$class: 'LabelParameterValue', name: 'node_to_run', label:nodeList[i]]]
                            
                            def mydata = [:]
                            mydata['job_JobName'] = job3.getProjectName()
                            mydata['job_BuildNumber'] = job3.getNumber()
                            mydata['job_NodeName'] = nodeList[i]
                            mydata['job_StartTime'] = job3.getStartTimeInMillis()
                            mydata['job_Duration'] = job3.getDuration()
                            mydata['job_Result'] = job3.getResult()
                            mydata['environment'] = envir
                            myList << mydata
                            
                            if(job3.getResult()!= 'SUCCESS'){
                                currentBuild.result = 'FAILURE'
                                //error (mydata['job_JobName'] + '#' + mydata['job_BuildNumber'] + ' Failed on node ' + nodeList[i])
                                error (mydata['job_JobName'] + ' Failed')
                            }else{
                                echo 'Build finished on node: ' + nodeList[i]
                            }
                        }
                    }
                    parallel(tasks)   
                }
            }
        }        

    }
    post {
            success{
                echo 'myList size: ' + myList.size()
                script{
                    currentBuild.result = "SUCCESS"
                    //for the jobs triggered
                    for (int i=0;i<myList.size(); i++){
                            step([$class: 'InfluxDbPublisher',
                            customData: myList[i],
                            customDataMap: null,
                            target: 'JOBS_DB'])
                    }
                    //for this pipeline
                    pipelineData =[:]
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = currentBuild.result
                    pipelineData['environment'] = envir

                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'PIPELINES_DB'])
                }
            }
            aborted {
                echo '<aborted message>'
                script{
                    for (int i=0;i<myList.size(); i++){
                        currentBuild.result = "ABORTED"
                        step([$class: 'InfluxDbPublisher',
                            customData: myList[i],
                            customDataMap: null,
                            target: 'JOBS_DB'])
                    }
                    pipelineData =[:]
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = currentBuild.result
                    pipelineData['environment'] = envir

                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'PIPELINES_DB'])
                }
            }
            failure {
                echo '<rollback process>'
                echo 'myList size: ' + myList.size()
                script{
                    for (int i=0;i<myList.size(); i++){
                        currentBuild.result = "FAILURE"
                        step([$class: 'InfluxDbPublisher',
                            customData: myList[i],
                            customDataMap: null,
                            target: 'JOBS_DB'])
                    }
                    pipelineData =[:]
                    pipelineData['upstream'] = upstream
                    pipelineData['result'] = currentBuild.result
                    pipelineData['environment'] = envir

                    step([$class: 'InfluxDbPublisher',
                        customData: pipelineData,
                        customDataMap: null,
                        target: 'PIPELINES_DB'])    
                }
            }
    }

}