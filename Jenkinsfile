pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3.10.1'
     }

    stages {
        stage("build") {
            steps {
                sh "mvn clean install"
            }
        }

        stage("test") {
            steps {
                sh "mvn test"
            }
        }
    }
}