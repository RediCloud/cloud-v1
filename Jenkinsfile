pipeline {
    agent any

    tools {
        jdk 'jdk-8'
        gradle 'gradle-7.4.2'
    }

    stages {
        stage("Permission") {
            steps {
                sh "su jenkins"
                sh "chmod +x ./gradlew";
            }
        }
        stage("Clean") {
            steps {
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
                sh "zip -r redi-cloud.zip test/node-1/*";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'redi-cloud.zip', fingerprint: true
                }
            }
        }
    }
}