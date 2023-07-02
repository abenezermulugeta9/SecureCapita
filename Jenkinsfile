pipeline {
    agent {
        docker {
            image 'maven:3.8.6-eclipse-temurin-11'
            args '-v /root/.m2:/root/.m2'
        }
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