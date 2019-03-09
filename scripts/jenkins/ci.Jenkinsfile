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
        SONAR_SCANNER_HOME = "${tool 'sonar-scanner'}"

        //GIT_CREDENTIALS_ID = "1d3b6329-988a-4970-8099-faf296d08c9b"

        // Build parameters
        DOCKER_REGISTRY_URL = "${env.DOCKER_REGISTRY_URL}"
        TARGET_ENV = "${env.TARGET_ENV}"
        GIT_REPO_BRANCH = "${env.GIT_REPO_BRANCH}"
        K8S_MASTER_SERVER_ACCOUNT = "${env.K8S_MASTER_SERVER_ACCOUNT}"
        K8S_MASTER_SERVER_IP = "${env.K8S_MASTER_SERVER_IP}"
        K8S_RES_IS_IGNORE_NOT_FOUND = "${env.K8S_RES_IS_IGNORE_NOT_FOUND}"
        K8S_VERIFY_STATUS_WAIT_TIME = "${env.K8S_VERIFY_STATUS_WAIT_TIME}"

//        BOOTSTRAP_CONFIG_URI = "http://10.252.127.164:31000/"
//        BOOTSTRAP_CONFIG_LABEL = "uat"
        BOOTSTRAP_CONFIG_URI = "${env.BOOTSTRAP_CONFIG_URI}"
        BOOTSTRAP_CONFIG_LABEL = "${env.BOOTSTRAP_CONFIG_LABEL}"


        GIT_REPO_PROJECT = "icp-isp-rep"
        //GIT_REPO_DIR = "${env.WORKSPACE}/${GIT_REPO_PROJECT}"
        //GIT_REPO_URL = "http://10.252.168.140/icp/${GIT_REPO_PROJECT}.git"

        SSH_CREDENTIALS_ID = "SSH-Jenkins-133"

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
                    DOCKER_IMAGE_VERSION = MAVEN_POM.version + "-" + env.BUILD_NUMBER

                    currentBuild.displayName = "Deploy " + MAVEN_POM.version + "-" + env.BUILD_NUMBER + " from " + GIT_REPO_BRANCH + " branch to " + TARGET_ENV + " env"
                }

                echo "Maven POM Version: " + MAVEN_POM.version
                echo "Docker Image Version: " + DOCKER_IMAGE_VERSION






                dir("src/main/docker") {
                    script {
                        echo "MAVEN_POM_VERSION = $MAVEN_POM_VERSION"
                        echo "DOCKER_IMAGE_VERSION = $DOCKER_IMAGE_VERSION"

                        // Set JAR version and Docker registry in Dockerfile
                        sh """sed -i "s/MAVEN_POM_VERSION/${MAVEN_POM_VERSION}/g" Dockerfile"""
                        sh """sed -i "s/DOCKER_REGISTRY_URL/${DOCKER_REGISTRY_URL}/g" Dockerfile"""
                        sh 'cat Dockerfile'

                        // Set Docker image version and Docker registry in K8S YAML
                        sh """sed -i "s/DOCKER_IMAGE_VERSION/${DOCKER_IMAGE_VERSION}/g" ${GIT_REPO_PROJECT}-${TARGET_ENV}.yaml"""
                        sh """sed -i "s/DOCKER_REGISTRY_URL/${DOCKER_REGISTRY_URL}/g" ${GIT_REPO_PROJECT}-${TARGET_ENV}.yaml"""
                        sh """cat ${GIT_REPO_PROJECT}-${TARGET_ENV}.yaml"""
                    }
                }
            }

        }

//        stage('Code Analysis') {
//            steps {
//                echo "Code Analysis stage"
//                withSonarQubeEnv('sonar6.4') {
//                sh """$SONAR_SCANNER_HOME/bin/sonar-scanner"""
//                }
//
//

//
//            }
//
//        }





        stage('Build and Test') {
            steps {

                // Run maven build and docker build, and push docker image to docker registry
                sh """
                        ${MAVEN_HOME}/bin/mvn clean install  \
                                                -Dmaven.test.skip=false \

                                                
                    """
            }
            post {
                always {
                    echo "Archive test results"
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

                    echo "Archive artifacts"
                    archive "target/**/*"
                }
            }
        }





    }

}