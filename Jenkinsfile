pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3.10.1'
     }

    stages {
        stage("compile") {
            steps {
                sh "mvn clean compile"
            }
        }

        stage("test") {
            steps {
                sh "mvn test"
            }
        }
    }
}