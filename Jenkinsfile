pipeline {
  agent any

  stages {
    stage("build") {
      steps {
        sh "mvn clean install"
        echo "application built"
      }
    }
    stage("test") {
      steps {
        sh "mvn test"
        echo "unit tests run"
      }
    }
    stage("deploy") {
      steps {
        echo "application deployed to aws..."
      }
    }
  }
}
