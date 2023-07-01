pipeline {
  agent any

  stages {
    stage("build") {
      steps {
        // sh "mvn clean install"
        echo "application built"
      }
    }
    stage("test") {
      steps {
        // sh "mvn clean install"
        echo "unit tests run"
      }
    }
    stage("deploy") {
      steps {
        // sh "mvn clean install"
        echo "application deployed to aws..."
      }
    }
  }
}
