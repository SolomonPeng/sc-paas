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
        DOCKER_REGISTRY_URL = "${env.DOCKER_REGISTRY_URL}"
        TARGET_ENV = "${env.TARGET_ENV}"
        GIT_REPO_BRANCH = "${env.GIT_REPO_BRANCH}"
        K8S_MASTER_SERVER_ACCOUNT = "${env.K8S_MASTER_SERVER_ACCOUNT}"
        K8S_MASTER_SERVER_URL = "${env.K8S_MASTER_SERVER_URL}"
        K8S_MASTER_SERVER_IP = "${env.K8S_MASTER_SERVER_IP}"
        K8S_RES_IS_IGNORE_NOT_FOUND = "${env.K8S_RES_IS_IGNORE_NOT_FOUND}"
        K8S_VERIFY_STATUS_WAIT_TIME = "${env.K8S_VERIFY_STATUS_WAIT_TIME}"
        K8S_REPLICAS = "${env.K8S_REPLICAS}"
        K8S_NAMESPACE = "${env.K8S_NAMESPACE}"
        K8S_NODEPORT = "${env.K8S_NODEPORT}"
        BOOTSTRAP_CONFIG_URI = "${env.BOOTSTRAP_CONFIG_URI}"
        BOOTSTRAP_CONFIG_LABEL = "${env.BOOTSTRAP_CONFIG_LABEL}"
        GIT_REPO_PROJECT = ""
        SSH_CREDENTIALS_ID = "${env.SSH_CREDENTIALS_ID}"

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
                sh """sed -i "s/DOCKER_REGISTRY_URL/${DOCKER_REGISTRY_URL}/g" pom.xml"""
                sh """sed -i "s!K8S_MASTER_SERVER_URL!${K8S_MASTER_SERVER_URL}!g" pom.xml"""
                sh """sed -i "s/K8S_NAMESPACE/${K8S_NAMESPACE}/g" pom.xml"""
                sh """sed -i "s/K8S_NODEPORT/${K8S_NODEPORT}/g" pom.xml"""
            }

        }

        stage('Version') {
            steps {

                script {
                    // readMavenPom method depends on Pipeline Utility Steps plugin
                    MAVEN_POM = readMavenPom file: 'pom.xml'

                    MAVEN_POM_VERSION = MAVEN_POM.version
                    DOCKER_IMAGE_VERSION = MAVEN_POM.version + "-" + env.BUILD_NUMBER
                    
                    GIT_REPO_PROJECT = MAVEN_POM.artifactId

                    currentBuild.displayName = "Deploy " + GIT_REPO_PROJECT + "-" + MAVEN_POM_VERSION + "-" + env.BUILD_NUMBER + " from " + GIT_REPO_BRANCH + " branch to " + TARGET_ENV + " env"
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

                        // Set Docker image version and Docker registry in K8S YAML
                        sh """sed -i "s/DOCKER_IMAGE_VERSION/${DOCKER_IMAGE_VERSION}/g" ${GIT_REPO_PROJECT}.yaml"""
                        sh """sed -i "s/DOCKER_REGISTRY_URL/${DOCKER_REGISTRY_URL}/g" ${GIT_REPO_PROJECT}.yaml"""
                        sh """sed -i "s!BOOTSTRAP_CONFIG_URI!${BOOTSTRAP_CONFIG_URI}!g" ${GIT_REPO_PROJECT}.yaml"""
                        sh """sed -i "s/BOOTSTRAP_CONFIG_LABEL/${BOOTSTRAP_CONFIG_LABEL}/g" ${GIT_REPO_PROJECT}.yaml"""
                        sh """sed -i "s/K8S_REPLICAS/${K8S_REPLICAS}/g" ${GIT_REPO_PROJECT}.yaml"""
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

//        stage('Test') {
//            steps {

                // Run Maven test
//                sh """
//                        ${MAVEN_HOME}/bin/mvn clean test \
//                                                -Dmaven.test.skip=false \
//                                                -Dconfig.uri=${BOOTSTRAP_CONFIG_URI} \
//                                               -Dconfig.label=${BOOTSTRAP_CONFIG_LABEL} \
//                                                -Dmaven.test.failure.ignore=false
//                    """
//            }
//            post {
//                always {
//                    echo "Archive test results"
//                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
//                }
//            }
//        }



        stage('Build') {
            steps {

                // Run maven build and docker build, and push docker image to docker registry
                sh """
                        ${MAVEN_HOME}/bin/mvn -P k8s clean install docker:build \
                                                -Dmaven.test.skip=true \
                                                -Ddocker.registry.address=${DOCKER_REGISTRY_URL} \
                                                -Ddocker.image.version=${DOCKER_IMAGE_VERSION} \
                                                -DpushImage
                    """
            }
            post {
                always {
                    echo "Archive artifacts"
                    archive "target/**/*"
                }
            }
        }


        stage('Deploy') {

//            input {
//                message "Are you confirm to deploy to ${TARGET_ENV} env?"
//                ok "Yes"
//            }

            steps {
                script {
                    SRC_DOCKER_DIR = "./target/docker/"
                    TMP_DOCKER_DIR = "/tmp/${GIT_REPO_PROJECT}/target/docker/"
                    sh """cat ${SRC_DOCKER_DIR}Dockerfile"""
                    sh """cat ${SRC_DOCKER_DIR}${GIT_REPO_PROJECT}.yaml"""
                    sshagent(['${SSH_CREDENTIALS_ID}']) {
                        // Depend on Jenkins SSH Agent plugin, and need set Jenkin's server SSH pub key in target env K8S master server
                        // Need ensure the ssh account have permission to run kubectl command
                        sh """
                                ssh -o StrictHostKeyChecking=no -l ${K8S_MASTER_SERVER_ACCOUNT} ${K8S_MASTER_SERVER_IP} 'mkdir -p ${TMP_DOCKER_DIR}'
                                scp ${SRC_DOCKER_DIR}*.yaml ${K8S_MASTER_SERVER_ACCOUNT}@${K8S_MASTER_SERVER_IP}:${TMP_DOCKER_DIR}
                                ssh -o StrictHostKeyChecking=no -l ${K8S_MASTER_SERVER_ACCOUNT} ${K8S_MASTER_SERVER_IP} '/usr/bin/kubectl delete -f ${TMP_DOCKER_DIR}${GIT_REPO_PROJECT}.yaml -s ${K8S_MASTER_SERVER_URL}  --ignore-not-found=${K8S_RES_IS_IGNORE_NOT_FOUND}'
                                ssh -o StrictHostKeyChecking=no -l ${K8S_MASTER_SERVER_ACCOUNT} ${K8S_MASTER_SERVER_IP} '/usr/bin/kubectl create -f ${TMP_DOCKER_DIR}${GIT_REPO_PROJECT}.yaml -s ${K8S_MASTER_SERVER_URL}'
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
                    sh """sleep ${K8S_VERIFY_STATUS_WAIT_TIME}"""

                    RUNNING_POD_COUNT = "0"
                    sshagent(['${SSH_CREDENTIALS_ID}']) {
                        RUNNING_POD_COUNT = sh (script: "ssh -o StrictHostKeyChecking=no -l ${K8S_MASTER_SERVER_ACCOUNT} ${K8S_MASTER_SERVER_IP} '/usr/bin/kubectl get pod -s ${K8S_MASTER_SERVER_URL} --namespace=${K8S_NAMESPACE} | grep ${GIT_REPO_PROJECT} | grep -i Running | wc -l'", returnStdout: true).trim()
                    }

                    echo "RUNNING_POD_COUNT=" + RUNNING_POD_COUNT

                    if (RUNNING_POD_COUNT == "0") {
                        echo "Verify Pod status failed! No Pod is running!"
                        failFast true
                    } else {

                        sh """
                            ssh -o StrictHostKeyChecking=no -l ${K8S_MASTER_SERVER_ACCOUNT} ${K8S_MASTER_SERVER_IP} 'cp -r ${TMP_DOCKER_DIR}${GIT_REPO_PROJECT}.yaml  /home/${K8S_NAMESPACE}/'

                            """
                        echo RUNNING_POD_COUNT + " Pods is running."
                    }

                }
            }

        }

    }

}
