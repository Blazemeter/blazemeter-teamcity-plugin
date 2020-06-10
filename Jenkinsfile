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
        stage('Install & Build Maven')
        {
            steps
            {
                sh """
                    apk add --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/v3.4/main/ maven;
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
