#!/usr/bin/env groovy

/**
 * Jenkins Pipeline
 *
 */

// Manual trigger
pipeline {

    agent any

    environment {
        MAVEN_HOME = "${tool 'maven'}"

        // Build parameters
        TARGET_ENV = "${env.TARGET_ENV}"
        GIT_REPO_BRANCH = "${env.GIT_REPO_BRANCH}"
        BOOTSTRAP_CONFIG_URI = "${env.BOOTSTRAP_CONFIG_URI}"
        BOOTSTRAP_CONFIG_LABEL = "${env.BOOTSTRAP_CONFIG_LABEL}"

        TARGET_SERVER_ACCOUNT = "${env.TARGET_SERVER_ACCOUNT}"
        TARGET_SERVER_IP = "${env.TARGET_SERVER_IP}"
        IS_SKIP_TEST = "${env.IS_SKIP_TEST}"


        GIT_REPO_PROJECT = "icp-erp-ws-erp"
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
                sh 'env'
                sh 'pwd'
                sh 'ls -ltra'

                // Clean workspace
                // The Jenkinsfile is checkout under workspace@script, so it won't be deleted when clean current workspace
                dir("${env.WORKSPACE}") {
                    deleteDir()
                }

                sh 'pwd'
                sh 'ls -ltra'

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

                // Run maven build and docker build, and push docker image to docker registry
                sh """
                        ${MAVEN_HOME}/bin/mvn clean install  \
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
                     // ps -ef | grep ${GIT_REPO_PROJECT} | grep java | grep -v grep | grep -v pipeline | awk -F ' ' '{print \$2}' | xargs sudo kill -9
                     sh """
                         cd ${DEPLOY_JAR_DIR}
                         rm -f ${GIT_REPO_PROJECT}*.jar
                         cp -p ${env.WORKSPACE}/target/${GIT_REPO_PROJECT}-${MAVEN_POM_VERSION}.jar ${DEPLOY_JAR_DIR}

                        """

                    // Stop application through kill process
                    PROCESS_ID = sh (script: "ps -ef | grep ${GIT_REPO_PROJECT} | grep java | grep jar | grep -v grep | grep -v pipeline | awk -F ' ' '{print \$2}'", returnStdout: true).trim()

                    echo "PROCESS_ID=" + PROCESS_ID

                    if (PROCESS_ID != "") {
                        sh """
                             echo "Kill process: ${PROCESS_ID}"
                             sudo kill -9 ${PROCESS_ID}
                            """
                    }


                     // Start application
                    withEnv(['JENKINS_NODE_COOKIE=background_job']) {
                        sh """
                            cd ${DEPLOY_JAR_DIR}
                            nohup java -jar -Dconfig.uri=${BOOTSTRAP_CONFIG_URI} -Dconfig.label=${BOOTSTRAP_CONFIG_LABEL} ${GIT_REPO_PROJECT}-${MAVEN_POM_VERSION}.jar > ${DEPLOY_LOG_FILE} &
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
                    sh """sleep ${VERIFY_STATUS_WAIT_TIME}"""

                    PROCESS_COUNT = sh (script: "ps -ef | grep ${GIT_REPO_PROJECT} | grep java | grep jar | grep -v grep | grep -v pipeline | wc -l", returnStdout: true).trim()

                    echo "PROCESS_COUNT=" + PROCESS_COUNT

                    // Can't use "failFast" method in Jenkins 2.107.3
                    if (PROCESS_COUNT == "1") {
                        echo "Verify successfully!"
                    } else if (PROCESS_COUNT == "0") {
                        echo "Verify failed! No specific process is running!"
                        sh """exit 1"""
                    } else {
                        echo "Verify failed! Found ${PROCESS_COUNT} duplicated processes."
                        sh """exit 1"""
                    }

                }
            }

        }

    }

}