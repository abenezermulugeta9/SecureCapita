pipeline {
    agent any

    tools {
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