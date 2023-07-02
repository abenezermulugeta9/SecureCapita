pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3.10.1'
     }

    stages {
        stage('Initialize'){
            def dockerHome = tool 'docker-latest'
            env.PATH = "${dockerHome}/bin:${env.PATH}"
        }
        stage("Compile") {
            steps {
                sh "mvn clean compile"
            }
        }

        stage("Dockerize") {
            steps {
                sh "docker ps"
            }
        }
    }
}
