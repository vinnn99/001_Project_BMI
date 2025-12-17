pipeline {
    agent any

    parameters {
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit tests')
        booleanParam(name: 'BUILD_APK', defaultValue: true, description: 'Build debug APK')
    }

    environment {
        ANDROID_SDK_ROOT = "/usr/lib/android-sdk"
        ANDROID_HOME     = "/usr/lib/android-sdk"
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        JAVA_OPTS        = "-Xmx2g"
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {
        stage('Checkout Source') {
            steps {
                echo 'Checking out source code'
                checkout scm
            }
        }

        stage('Check Environment') {
            steps {
                sh '''
                    echo "Java Version:" 
                    java -version
                    echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
                    ls -la
                '''
            }
        }

        stage('Setup Gradle Wrapper') {
            steps {
                sh '''
                    chmod +x ./gradlew
                    ./gradlew --version
                '''
            }
        }

        stage('Run Unit Tests') {
            when {
                expression { params.RUN_TESTS }
            }
            steps {
                echo 'Running unit tests'
                sh '''
                    ./gradlew test --info
                '''
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
                failure {
                    echo 'Unit tests failed'
                }
            }
        }

        stage('Build Debug APK') {
            when {
                expression { params.BUILD_APK }
            }
            steps {
                echo 'Building debug APK'
                sh '''
                    ./gradlew clean assembleDebug --info
                '''
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '**/build/outputs/apk/**/*.apk', allowEmptyArchive: true
                archiveArtifacts artifacts: '**/build/test-results/**/*.xml', allowEmptyArchive: true
            }
        }
    }

    post {
        success {
            echo 'Pipeline SUCCESS – APK ready'
        }
        failure {
            echo 'Pipeline FAILED – check logs'
        }
        always {
            echo "Build #${BUILD_NUMBER} finished with status: ${currentBuild.currentResult}"
        }
    }
}
