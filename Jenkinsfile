clearWorkspaceAsRoot()
@Library ("jenkins_library@WAP-15611-migrate-jenkins-GKE") _

pipeline
{
    agent {
       kubernetes {
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
        stage('Build Maven')
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
