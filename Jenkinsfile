pipeline {
    agent any

    tools {
        jdk 'jdk-8'
        gradle 'gradle-7.4.2'
    }

    stages {
        stage("Clean") {
            steps {
                sh "chmod +x ./gradlew";
                sh "./gradlew clean";
            }
        }
        stage("Build") {
            steps {
                sh "./gradlew buildAndCopy";
            }
        }
        stage("Create zip") {
            steps {
                sh "cd test/node-1/; zip -r redi-cloud.zip *";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'test/node-1/redi-cloud.zip', fingerprint: true
                }
            }
        }
    }
}