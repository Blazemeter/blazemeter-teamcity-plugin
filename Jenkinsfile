clearWorkspaceAsRoot()
@Library ("jenkins_library@WAP-15611-migrate-jenkins-GKE") _

pipeline
{
    /*agent
    {
        docker
        {
            image 'maven:3.5.0-jdk-8'
            args '-u root'
        }
    }*/
    agent
    {
       kubernetes
       {
           label 'jenkins-agent-common'
           defaultContainer 'jenkins-agent-common'
       }
    }
    options
    {
        buildDiscarder(logRotator(daysToKeepStr: '20',numToKeepStr: '50'))
        ansiColor('xterm')
        timestamps()
    }
    stages
    {
        stage('Install & Build Maven')
        {
            steps
            {
                sh """
                    mvn clean install -Djenkins.build.number=${BUILD_NUMBER}
                    """
            }
        }
    }
    post
    {
        always
        {
            archiveArtifacts artifacts: 'target/BlazeMeter.zip', onlyIfSuccessful: true
        }
    }
}
