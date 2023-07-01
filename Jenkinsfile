pipeline {
  agent any

  stages {
    stage("build") {
      steps {
        sh "mvn clean install"
      }
      steps ("test") {
        sh "mvn test"
      }
    }
  }
}
