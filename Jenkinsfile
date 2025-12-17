pipeline {
    agent any

    parameters {
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit tests')
        booleanParam(name: 'BUILD_APK', defaultValue: true, description: 'Build debug APK')
    }

    environment {
        ANDROID_SDK_ROOT = "${WORKSPACE}/android-sdk"
        ANDROID_HOME     = "${WORKSPACE}/android-sdk"
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

        stage('Prepare Android SDK') {
            steps {
                echo 'Setting up local Android SDK (cmdline-tools, platforms, build-tools)'
                sh '''
                    set -e
                    SDK_ROOT="$WORKSPACE/android-sdk"
                    mkdir -p "$SDK_ROOT"

                    TOOLS_ZIP="$WORKSPACE/cmdline-tools.zip"
                    TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip"
                    if command -v curl >/dev/null 2>&1; then
                      curl -L -o "$TOOLS_ZIP" "$TOOLS_URL"
                    else
                      wget -q -O "$TOOLS_ZIP" "$TOOLS_URL"
                    fi

                    mkdir -p "$SDK_ROOT/cmdline-tools"
                    unzip -qo "$TOOLS_ZIP" -d "$SDK_ROOT/cmdline-tools"
                    # Move into expected 'latest' directory name
                    if [ -d "$SDK_ROOT/cmdline-tools/cmdline-tools" ]; then
                      mv "$SDK_ROOT/cmdline-tools/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"
                    fi

                    # Accept licenses and install required components locally
                    yes | "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK_ROOT" \
                        "platform-tools" \
                        "platforms;android-36" \
                        "build-tools;35.0.0"

                    "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK_ROOT" --list | head -n 50 || true
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
