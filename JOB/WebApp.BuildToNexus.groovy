node('master'){
stage('BuildJob') {
  checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'GitAccount', url: 'https://github.com/TechNetDemo/WebAppDemo.git']]])
  sh 'mvn -f webapp-team1/pom.xml clean compile package'
  
  def pom = readMavenPom file: 'webapp-team1/pom.xml'
  nexusPublisher nexusInstanceId: 'NexusRepo', \
  nexusRepositoryId: 'WEBAPP', \
  packages: [[$class: 'MavenPackage', \
  mavenAssetList: [[classifier: '', extension: '', \
  filePath: "${pom.artifactId}/target/${pom.artifactId}.${pom.packaging}"]], \
  mavenCoordinate: [artifactId: "${pom.artifactId}", \
  groupId: "${pom.groupId}", \
  packaging: "${pom.packaging}", \
  version: "${pom.version}"]]]
}
}