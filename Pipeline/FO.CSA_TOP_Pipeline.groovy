pipeline {
    agent none
    parameters {
        string(name: 'artifact_version', defaultValue: '0.1.0-SNAPSHOT', description: 'Artifact Built to JFrog')
    }
    stages {
        stage('DEV') { 
            input{ 
                message 'param submission (by env_admin)'
                submitter 'dev_admin,admin'
                parameters{
                    string(name: 'db_username', defaultValue: 'root', description: 'account for WebApp connect to MySQL')
                    password(name: 'db_password', defaultValue: '',description: 'password of the account')
                }  
            }
            steps {
                input message: 'Start DEV Deployment? (by approver)', submitter: 'admin,dev_admin'
                build job: '/FO.CSA/DEV/FO.CSA_DEV_Pipeline', parameters: [string(name: 'artifact_version', value: artifact_version), string(name: 'upstream', value: JOB_NAME), string(name: 'db_username', value: db_username),[$class: 'com.michelin.cio.hudson.plugins.passwordparam.PasswordParameterValue', name: 'db_password', value: db_password]]
            }
        }
        stage('SIT') { 
            input{ 
                message 'param submission(by env_admin)'
                submitter 'sit_admin,admin'
                parameters{
                    string(name: 'db_username', defaultValue: 'root', description: 'account for WebApp connect to MySQL')
                    password(name: 'db_password', defaultValue: '',description: 'password of the account')
                }  
            }
            steps {
                input message: 'Start SIT Deployment?(by approver)'
                build job:'/FO.CSA/SIT/FO.CSA_SIT_Pipeline', parameters: [string(name: 'artifact_version', value: artifact_version), string(name: 'upstream', value: JOB_NAME), string(name: 'db_username', value: db_username),[$class: 'com.michelin.cio.hudson.plugins.passwordparam.PasswordParameterValue', name: 'db_password', value: db_password]]
            }
        }
        stage('SAT') { 
            input{ 
                message 'param submission (by env_admin)'
                submitter 'sat_admin,admin'
                parameters{
                    string(name: 'db_username', defaultValue: 'root', description: 'account for WebApp connect to MySQL')
                    password(name: 'db_password', defaultValue: '',description: 'password of the account')
                }  
            }
            steps {
                input message: 'Start SAT Deployment? (by approver)'    
                build job:'/FO.CSA/SAT/FO.CSA_SAT_Pipeline', parameters: [string(name: 'artifact_version', value: artifact_version), string(name: 'upstream', value: JOB_NAME), string(name: 'db_username', value: db_username),[$class: 'com.michelin.cio.hudson.plugins.passwordparam.PasswordParameterValue', name: 'db_password', value: db_password]]
            }
        }
        stage('PROD') { 
            input{ 
                message 'param submission(by env_admin)'
                submitter 'prod_admin,admin'
                parameters{
                    string(name: 'db_username', defaultValue: 'root', description: 'account for WebApp connect to MySQL')
                    password(name: 'db_password', defaultValue: '',description: 'password of the account')
                }  
            }
            steps {
                input message: 'Start PROD Deployment? (by approver)'   
                build job:'/FO.CSA/PROD/FO.CSA_PROD_Pipeline', parameters: [string(name: 'artifact_version', value: artifact_version), string(name: 'upstream', value: JOB_NAME), string(name: 'db_username', value: db_username),[$class: 'com.michelin.cio.hudson.plugins.passwordparam.PasswordParameterValue', name: 'db_password', value: db_password]]
            }
        }
    }

}