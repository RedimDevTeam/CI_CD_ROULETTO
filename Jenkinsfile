
pipeline {

    agent any

    environment {
        APP_NAME = "rouletto"
        DEPLOY_DIR = "/opt/rouletto"
        JAR_NAME = "rouletto.jar"
    }

    stages {

        stage('Check Environment') {
            steps {
                echo "===== Checking Java ====="
                sh 'java -version'

                echo "===== Checking Maven ====="
                sh 'mvn -version'

                echo "===== Checking Git ====="
                sh 'git --version'
            }
        }

        stage('Checkout') {
            steps {
                echo "===== Checking out source code ====="
                checkout scm

                echo "===== Current Git Commit ====="
                sh 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                echo "===== Building application ====="

                dir('rouletto') {
            sh 'mvn clean install -DskipTests'
              }
            }
        }

     

        stage('Find JAR') {
            steps {
                echo "===== Generated JAR files ====="

                sh '''
                    find target -name "*.jar" -type f -print
                '''
            }
        }

        stage('Prepare Deployment Directory') {
            steps {
                echo "===== Preparing deployment directory ====="

                sh '''
                    sudo mkdir -p ${DEPLOY_DIR}
                    sudo chown -R jenkins:jenkins ${DEPLOY_DIR}
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo "===== Deploying application ====="

                sh '''
                    JAR_FILE=$(find target -maxdepth 1 -name "*.jar" -type f | head -1)

                    if [ -z "$JAR_FILE" ]; then
                        echo "ERROR: JAR file not found"
                        exit 1
                    fi

                    echo "Found JAR: $JAR_FILE"

                    sudo cp "$JAR_FILE" "${DEPLOY_DIR}/${JAR_NAME}"

                    sudo chown jenkins:jenkins "${DEPLOY_DIR}/${JAR_NAME}"

                    echo "JAR deployed successfully"
                '''
            }
        }

        stage('Restart Application') {
            steps {
                echo "===== Restarting application ====="

                sh '''
                    sudo systemctl restart ${APP_NAME}

                    sleep 5

                    sudo systemctl status ${APP_NAME} --no-pager
                '''
            }
        }

        stage('Health Check') {
            steps {
                echo "===== Checking application ====="

                sh '''
                    if sudo systemctl is-active --quiet ${APP_NAME}; then
                        echo "===================================="
                        echo "Application is RUNNING"
                        echo "===================================="
                    else
                        echo "===================================="
                        echo "Application FAILED TO START"
                        echo "===================================="

                        sudo journalctl -u ${APP_NAME} -n 50 --no-pager

                        exit 1
                    fi
                '''
            }
        }
    }

    post {

        success {
            echo "======================================"
            echo "CI/CD PIPELINE SUCCESSFUL"
            echo "Application: ${APP_NAME}"
            echo "======================================"
        }

        failure {
            echo "======================================"
            echo "CI/CD PIPELINE FAILED"
            echo "======================================"
        }

        always {
            echo "Cleaning Jenkins workspace"

            sh '''
                echo "Build completed"
            '''
        }
    }
}

