// Jenkins Declarative Pipeline for Android BMI Calculator App
// ============================================================
// This pipeline automates the build, test, and deployment process
// for an Android application written in Kotlin and Gradle.
//
// Pipeline Flow:
// 1. Checkout source code from Git
// 2. Check Java installation
// 3. Setup Gradle wrapper permissions
// 4. Run unit tests (fails pipeline if tests fail)
// 5. Build debug APK
// 6. Archive build artifacts

pipeline {
    // Use any available agent/node
    agent any

    // Define pipeline parameters (optional)
    parameters {
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Run unit tests before building'
        )
        booleanParam(
            name: 'BUILD_APK',
            defaultValue: true,
            description: 'Build debug APK after tests pass'
        )
    }

    // Environment variables for the pipeline
        environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        JAVA_OPTS = "-Xmx2g"
        ANDROID_SDK_ROOT = "/usr/lib/android-sdk"
        ANDROID_HOME = "/usr/lib/android-sdk"
    }

    // Pipeline triggers (optional)
    triggers {
        // Trigger on GitHub push (requires webhook setup)
        githubPush()
        
        // Alternative: Poll SCM every 15 minutes
        // pollSCM('H/15 * * * *')
    }

    // Build options
    options {
        // Keep builds for 30 days
        buildDiscarder(logRotator(numToKeepStr: '30'))
        
        // Timeout after 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        
        // Add timestamps to console output
        timestamps()
    }

    stages {
        // ============================================================
        // STAGE 1: Checkout Source Code
        // ============================================================
        stage('Checkout') {
            steps {
                script {
                    echo '================================'
                    echo 'STAGE 1: Checking out source code'
                    echo '================================'
                }
                
                // Checkout from Git repository
                checkout scm
                
                echo "✓ Source code checked out successfully"
            }
        }

        // ============================================================
        // STAGE 2: Check Environment
        // ============================================================
        stage('Check Environment') {
            steps {
                script {
                    echo '================================'
                    echo 'STAGE 2: Checking build environment'
                    echo '================================'
                }
                
                // Check Java installation and version
                sh '''
                    echo "Java Version:"
                    java -version
                    echo ""
                    echo "Current Directory: $(pwd)"
                    echo "Workspace: ${WORKSPACE}"
                    echo ""
                    echo "Gradle wrapper status:"
                    ls -la gradlew || echo "gradlew not found"
                '''
            }
        }

        // ============================================================
        // STAGE 3: Setup Gradle
        // ============================================================
        stage('Setup Gradle') {
            steps {
                script {
                    echo '================================'
                    echo 'STAGE 3: Setting up Gradle wrapper'
                    echo '================================'
                }
                
                // Grant execute permission to gradle wrapper
                sh '''
                    chmod +x ./gradlew
                    echo "✓ Execute permission granted to ./gradlew"
                    
                    # Show gradle version
                    ./gradlew --version
                '''
            }
        }

        // ============================================================
        // STAGE 4: Run Unit Tests
        // ============================================================
        stage('Unit Tests') {
            when {
                // Only run if RUN_TESTS parameter is true
                expression { params.RUN_TESTS == true }
            }
            steps {
                script {
                    echo '================================'
                    echo 'STAGE 4: Running unit tests'
                    echo '================================'
                }
                
                // Accept Android SDK licenses automatically
                sh '''
                    echo "Accepting Android SDK licenses..."
                    # Create license files in workspace first
                    mkdir -p ./licenses
                    
                    # Main SDK license
                    echo -e "\\n8933bad161af4d5d5a97f8ba6f179d5afc0a28f8" > ./licenses/android-sdk-license
                    
                    # Preview license
                    printf "\\n84861d99f48726d6882ecb687bab996e17f0ed39d\\nd56f5187479451eabf01fb78af6dfcb131b33968f\\n" > ./licenses/android-sdk-preview-license
                    
                    # Ensure directory exists
                    sudo mkdir -p /usr/lib/android-sdk/licenses
                    
                    # Copy all licenses
                    sudo cp ./licenses/android-sdk-license /usr/lib/android-sdk/licenses/
                    sudo cp ./licenses/android-sdk-preview-license /usr/lib/android-sdk/licenses/
                    
                    # Verify licenses
                    echo "✓ Licenses configured"
                    echo "Installed licenses:"
                    ls -la /usr/lib/android-sdk/licenses/ || true
                '''
                
                // Run Gradle test task
                sh '''
                    echo "Executing unit tests..."
                    ./gradlew test --info
                    
                    # Check test results
                    if [ $? -eq 0 ]; then
                        echo "✓ All unit tests passed successfully"
                    else
                        echo "✗ Unit tests failed"
                        exit 1
                    fi
                '''
            }
            
            // Post actions after tests
            post {
                always {
                    // Publish test results
                    junit '**/build/test-results/test/*.xml'
                    
                    // Generate test report
                    script {
                        echo "Test results published"
                    }
                }
                
                failure {
                    // Send failure notification
                    script {
                        echo "❌ TESTS FAILED - Build aborted"
                    }
                }
            }
        }

        // ============================================================
        // STAGE 5: Build Debug APK
        // ============================================================
        stage('Build APK') {
            when {
                // Only build if unit tests passed and BUILD_APK is true
                expression { params.BUILD_APK == true }
            }
            steps {
                script {
                    echo '================================'
                    echo 'STAGE 5: Building debug APK'
                    echo '================================'
                }
                
                // Assemble debug build
                sh '''
                    echo "Building debug APK..."
                    ./gradlew clean assembleDebug --info
                    
                    # Check build status
                    if [ $? -eq 0 ]; then
                        echo "✓ APK built successfully"
                        echo ""
                        echo "APK location:"
                        find . -name "*.apk" -type f
                    else
                        echo "✗ APK build failed"
                        exit 1
                    fi
                '''
            }
        }

        // ============================================================
        // STAGE 6: Archive Artifacts
        // ============================================================
        stage('Archive Artifacts') {
            steps {
                script {
                    echo '================================'
                    echo 'STAGE 6: Archiving build artifacts'
                    echo '================================'
                }
                
                // Archive APK files
                archiveArtifacts artifacts: '**/build/outputs/apk/**/*.apk', 
                                 allowEmptyArchive: true
                
                // Archive test reports
                archiveArtifacts artifacts: '**/build/test-results/**/*.xml',
                                 allowEmptyArchive: true
                
                echo "✓ Artifacts archived successfully"
            }
        }
    }

    // ============================================================
    // POST BUILD ACTIONS
    // ============================================================
    post {
        always {
            // Clean workspace (optional)
            script {
                echo "Pipeline execution completed"
            }
            
            // Publish build info
            script {
                echo "Build: ${BUILD_NUMBER}"
                echo "Status: ${currentBuild.result}"
                echo "Duration: ${currentBuild.durationString}"
            }
        }
        
        success {
            script {
                echo "✓ Pipeline completed successfully"
                // Optional: Send success notification
                // mail to: 'team@example.com',
                //      subject: "Build #${BUILD_NUMBER} successful",
                //      body: "Build successful. APK ready for testing."
            }
        }
        
        failure {
            script {
                echo "✗ Pipeline failed"
                // Optional: Send failure notification
                // mail to: 'team@example.com',
                //      subject: "Build #${BUILD_NUMBER} failed",
                //      body: "Build failed. Check logs for details."
            }
        }
        
        unstable {
            script {
                echo "⚠ Pipeline unstable"
            }
        }
        
        cleanup {
            script {
                echo "Cleaning up workspace..."
                // Optional: Clean build directory
                // deleteDir()
            }
        }
    }
}

// ============================================================
// NOTES FOR SETUP:
// ============================================================
// 1. Place this file as 'Jenkinsfile' in project root
// 2. Configure Jenkins Job:
//    - New Pipeline Job
//    - Pipeline -> Definition: Pipeline script from SCM
//    - SCM: Git
//    - Repository URL: your-repo-url
//    - Branch: */main (or your branch)
// 3. Configure GitHub Webhook (optional):
//    - GitHub Settings -> Webhooks
//    - Payload URL: http://jenkins-url/github-webhook/
// 4. Requirements:
//    - Java 8 or higher
//    - Android SDK (for full builds)
//    - Git
// ============================================================
