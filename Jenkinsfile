// Declarative Jenkins pipeline. Expects JDK and Maven tools configured in Jenkins.
// API credentials come from the Jenkins credential store, never from the repo.
pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    parameters {
        choice(name: 'SUITE', choices: ['regression', 'smoke'], description: 'TestNG suite to run')
        string(name: 'API_BASE_URI', defaultValue: 'https://restful-booker.herokuapp.com', description: 'Environment to test')
    }

    options {
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('API tests') {
            environment {
                // Create a "Username with password" credential with this ID in Jenkins.
                API_CREDS = credentials('booking-api-credentials')
            }
            steps {
                sh '''
                    mvn -B clean test \
                      -Dsuite=${SUITE} \
                      -Dapi.base.uri=${API_BASE_URI} \
                      -Dapi.username=${API_CREDS_USR} \
                      -Dapi.password=${API_CREDS_PSW}
                '''
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
            archiveArtifacts artifacts: 'target/surefire-reports/**', allowEmptyArchive: true
        }
    }
}
