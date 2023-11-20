pipeline {
    agent {
        docker {
            registryUrl 'https://us-docker.pkg.dev'
            image 'verdant-bulwark-278/bzm-plugin-base-image/bzm-plugin-base-image:latest'
            registryCredentialsId 'push-to-gar-enc'
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock -v $WORKSPACE:/build'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: "10"))
        ansiColor('xterm')
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        JETBRAIN-TOKEN = credentials('blazerunner_jetbrains_token')
    }

    stages {
        stage('Build Release') {
            steps {
                script {
                    sh'''
                    mvn clean install
                    '''
                }
            }
        }
        stage('Publish Release') {
            steps {
                script {
                    sh"""
                    curl -i --header "Authorization: Bearer ${JETBRAIN-TOKEN}" -F pluginId=BlazeMeter -F file=@target/BlazeMeter.zip -F channel=Stable https://plugins.jetbrains.com/plugin/uploadPlugin
                    """
                }
            }
        }
    }
}
