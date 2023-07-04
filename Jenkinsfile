pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3.10.1'
     }

    stages {
        stage("Compile") {
            steps {
                sh "mvn clean compile"
            }
        }

        stage("Build") {
            steps {
                sh "mvn clean package -DskipTests=true"
            }
        }
    }
}