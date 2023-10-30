import java.nio.file.Path
import java.nio.file.Files

pipeline {
    agent any
    stages {
        stage("Build") {
            steps {
                sh "./gradlew clean assemble build"
                junit testResults: "build/test-results/test/*.xml"
                javadoc javadocDir: "build/docs/javadoc", keepAll: true
                archiveArtifacts artifacts: 'build/libs/*.jar,benchmark/build/libs/*.jar,wson-cli/build/distributions/wson-cli-envelope-*.jar,wson-cli/build/distributions/wson-cli',
                                 allowEmptyArchive: true,
                                 fingerprint: true,
                                 onlyIfSuccessful: true
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publish"
            }
        }
    }
}

