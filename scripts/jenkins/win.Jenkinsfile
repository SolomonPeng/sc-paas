#!/usr/bin/env groovy

/**
 * Jenkins Pipeline
 *
 */

// Manual trigger
pipeline {

    // Run in Windows agent
    agent { label 'windows' }

    environment {
        MAVEN_HOME = "${tool 'maven_windows'}"
        JAVA_HOME = "${tool 'java_windows'}"

        // Build parameters
        TARGET_ENV = "${env.TARGET_ENV}"
        GIT_REPO_BRANCH = "${env.GIT_REPO_BRANCH}"
        BOOTSTRAP_CONFIG_URI = "${env.BOOTSTRAP_CONFIG_URI}"
        BOOTSTRAP_CONFIG_LABEL = "${env.BOOTSTRAP_CONFIG_LABEL}"

        TARGET_SERVER_ACCOUNT = "${env.TARGET_SERVER_ACCOUNT}"
        TARGET_SERVER_IP = "${env.TARGET_SERVER_IP}"
        IS_SKIP_TEST = "${env.IS_SKIP_TEST}"


        GIT_REPO_PROJECT = "cpms-svc"
        SSH_CREDENTIALS_ID = "${env.SSH_CREDENTIALS_ID}"
        VERIFY_STATUS_WAIT_TIME = "${env.VERIFY_STATUS_WAIT_TIME}"

        DEPLOY_JAR_DIR="${env.DEPLOY_JAR_DIR}"
        DEPLOY_LOG_FILE="${env.DEPLOY_LOG_FILE}"

    }

    // Don't use tools, because it will auto-install the tools in each stage
//    tools {
//        // Symbol for tool type and then name of configured tool installation
//        maven "maven"
//    }


    stages {
        stage('Initialize') {
            steps {

                // [Debug] Print ENV variables
                bat 'set'
                bat 'cd'
                bat 'dir'

                // Clean workspace
                // The Jenkinsfile is checkout under workspace@script, so it won't be deleted when clean current workspace
                dir("${env.WORKSPACE}") {
                    deleteDir()
                }

                bat 'cd'
                bat 'dir'

            }
        }

        stage('Checkout') {
            steps {
                // Check out code from specific branch of GitLab repository
                //git branch: "${GIT_REPO_BRANCH}", credentialsId: "${GIT_CREDENTIALS_ID}", url: "${GIT_REPO_URL}"
                checkout scm
            }

        }

        stage('Version') {
            steps {

                script {
                    // readMavenPom method depends on Pipeline Utility Steps plugin
                    MAVEN_POM = readMavenPom file: 'pom.xml'

                    MAVEN_POM_VERSION = MAVEN_POM.version

                    currentBuild.displayName = "Deploy " + MAVEN_POM.version + "-" + env.BUILD_NUMBER + " from " + GIT_REPO_BRANCH + " branch to " + TARGET_ENV + " env"
                }

                echo "Maven POM Version: " + MAVEN_POM.version
            }

        }

        stage('Build & Test ') {
            steps {

                // Run maven build

                bat """
                        ${MAVEN_HOME}/bin/mvn clean install deploy \
                                                -Dmaven.test.skip=${IS_SKIP_TEST} \
                                                -Dconfig.uri=${BOOTSTRAP_CONFIG_URI} \
                                                -Dconfig.label=${BOOTSTRAP_CONFIG_LABEL} \
                                                -Dmaven.test.failure.ignore=${IS_SKIP_TEST}
                    """
            }
            post {
                always {
                    echo "Archive artifacts"
                    archiveArtifacts "target/**/*"

                    echo "Archive test results"
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

                }
            }
        }


        stage('Deploy') {

//            input {
//                message "Are you confirm to deploy to ${TARGET_ENV} env: ${TARGET_SERVER_IP} ?"
//                ok "Yes"
//            }

            steps {
                script {
                     bat """
                         cd /D ${env.WORKSPACE}\\target
                         copy /Y ${GIT_REPO_PROJECT}-${MAVEN_POM_VERSION}.jar ${DEPLOY_JAR_DIR}

                        """

                    // Stop application through kill process
                    // Need use "%%" to escape "%"
                    bat """
                        wmic process where "caption='java.exe' and commandline like '%%${GIT_REPO_PROJECT}%%'" call Terminate
                        """

                     // Start application
                    withEnv(['JENKINS_NODE_COOKIE=background_job']) {
                        bat """
                            cd /D ${DEPLOY_JAR_DIR}
                            start /b java -jar -Dconfig.uri=${BOOTSTRAP_CONFIG_URI} -Dconfig.label=${BOOTSTRAP_CONFIG_LABEL} ${GIT_REPO_PROJECT}-${MAVEN_POM_VERSION}.jar > ${DEPLOY_LOG_FILE}
                           """
                    }

                }
            }
            post {
                failure {
                    echo "Failed to deploy to ${TARGET_ENV} env!"
                }
                success {
                    echo "Deploy to ${TARGET_ENV} env successfully!"
                }
            }

        }

        stage('Verify after deployment') {
            steps {
                script {
                    // Wait for a while to perform verify
                    // Use ping to simulate sleep roughly
                    bat """ping 127.0.0.1 -n ${VERIFY_STATUS_WAIT_TIME} >nul """

                    // Need use "%%" to escape "%"
                    // If no running process, it will fail
                    RUNNING_PROCESS = bat (script: "wmic process where \"caption='java.exe' and commandline like '%%${GIT_REPO_PROJECT}%%'\" get processId/value | find /c \"ProcessId\"", returnStdout: true).trim()

                    echo "RUNNING_PROCESS: " + RUNNING_PROCESS

                }
            }

        }

    }

}