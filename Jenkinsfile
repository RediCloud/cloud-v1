pipeline {
    agent any

    tools {
        jdk 'jdk-8'
        gradle 'gradle-7.4.2'
    }

    stages {
        stage("Build") {
            steps {
                sh "chmod +x ./gradlew";
                sh "./gradlew projectBuild";
            }
        }
        stage("Create zip") {
            steps {
                sh "mkdir build/"
                sh "cd build"
                sh "cp ../test/node-1/storage/ storage/"
                sh "cp ../node/node-base/build/libs/*"
                sh "cp ../plugins/plugin-minecraft/build/libs/* storage/"
                sh "cp ../plugins/plugin-proxy/build/libs/* storage/"
                sh "zip -r redi-cloud.zip *";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/redi-cloud.zip', fingerprint: true
                }
            }
        }
        stage("Delete temp files") {
            steps {
                sh "rm -r build"
            }
        }
    }
}