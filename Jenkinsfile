pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3.10.1'
        docker 'docker:latest'
     }

    stages {
        stage("Compile") {
            steps {
                sh "mvn clean compile"
            }
        }

        stage("Dockerizing") {
            sh "docker ps"
        }
    }
}
