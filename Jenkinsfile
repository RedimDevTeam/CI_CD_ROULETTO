pipeline {

    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Deploy') {
            steps {
                sh '''
                    cp target/*.jar /opt/rouletto/
                '''
            }
        }
        stage('Restart') {
            steps {
                sh '''
                    sudo systemctl restart rouletto
                '''
            }
        }
    }
}
