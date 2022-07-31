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
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh "mkdir build/"
                    sh "cp test/node-1/storage/ build/storage/"
                    sh "cp node/node-base/build/libs/* build/"
                    sh "cp plugins/plugin-minecraft/build/libs/* build/storage/"
                    sh "cp plugins/plugin-proxy/build/libs/* build/storage/"
                    sh "cd build/; zip -r redi-cloud.zip *";
                }
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