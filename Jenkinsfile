pipeline {
    agent any

    tools {
        jdk 'jdk-8'
        gradle 'gradle-7.4.2'
    }

    stages {
        stage("Build") {
            steps {
                sh "./gradlew projectBuild --stacktrace --parallel --daemon";
            }
        }
        stage("Create zip") {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh "mkdir build/"
                    sh "cp -r test/node-1/storage/ build/storage/"
                    sh "cp node/node-base/build/libs/redicloud-node-base.jar build/"
                    sh "cp plugins/plugin-minecraft/build/libs/redicloud-plugin-minecraft.jar build/storage/"
                    sh "cp plugins/plugin-bungeecord/build/libs/redicloud-plugin-bungeecord.jar build/storage/"
                    sh "cp plugins/plugin-velocity/build/libs/redicloud-plugin-velocity.jar build/storage/"
                    sh "cd build/storage/; mkdir versions"
                    sh "cp limbo-server/build/libs/redicloud-limbo-server.jar build/storage/versions/limbo.jar"
                    sh "cp test/node-1/start.sh build/"
                    sh "cp test/node-1/start.bat build/"
                    sh "cp test/node-1/start_debug.sh build/"
                    sh "cp test/node-1/start_debug.bat build/"
                    sh "cd build; tar cfv redi-cloud.zip *"
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/redi-cloud.zip', fingerprint: true
                }
            }
        }
        stage("Publishing") {
            steps {
                sh "./gradlew publishToRepository --stacktrace";
            }
        }
        stage("Delete temp files") {
            steps {
                sh "rm -r build"
            }
        }
    }
}
