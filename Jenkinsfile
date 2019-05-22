clearWorkspaceAsRoot()
@Library ("jenkins_library") _

pipeline
{
    agent
    {
        docker
        {
            image 'maven:3.5.0-jdk-8'
            args '-u root'
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
}
