
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

                   dir('api-response') {
                 sh 'mvn clean install -DskipTests -e'
              }
                   dir('helper') {
                sh 'mvn clean install -DskipTests'
              }
                  dir('jwt') {
                  sh 'mvn clean install -DskipTests'
               }
                dir('rouletto') {
            sh 'mvn clean install -DskipTests'
              }
            }
        }

stage('Find JAR') {
    steps {
        echo "===== Generated JAR files ====="

        dir('rouletto') {
            sh '''
                find target -maxdepth 1 -name "*.jar" -type f -print
            '''
        }
    }
}

stage('Deploy') {
    steps {
        echo "===== Deploying Rouletto ====="

        dir('rouletto') {
            sh '''
                JAR_FILE=$(find target -maxdepth 1 -name "*.jar" -type f | head -1)

                if [ -z "$JAR_FILE" ]; then
                    echo "ERROR: JAR file not found"
                    exit 1
                fi

                echo "Found JAR: $JAR_FILE"

                sudo mv "$JAR_FILE" /opt/rouletto/rouletto.jar

                echo "JAR moved to /opt/rouletto/rouletto.jar"
            '''
        }
    }
}

stage('Restart Application') {
    steps {
        echo "===== Restarting Rouletto ====="

        sh '''
            sudo systemctl restart rouletto

            sleep 5

            sudo systemctl status rouletto --no-pager
        '''
    }
}

stage('Health Check') {
    steps {
        echo "===== Health Check ====="

        sh '''
            if sudo systemctl is-active --quiet rouletto; then
                echo "===================================="
                echo "ROULETTO IS RUNNING"
                echo "===================================="
            else
                echo "===================================="
                echo "ROULETTO FAILED TO START"
                echo "===================================="

                sudo journalctl -u rouletto -n 50 --no-pager

                exit 1
            fi
        '''
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

