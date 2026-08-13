pipeline {
  agent any

  options {
    ansiColor('xterm')
    skipDefaultCheckout(true)
  }

  parameters {
    string(name: 'AWS_ACCOUNT_ID', defaultValue: '', description: 'AWS account ID used for ECR/ECS')
    string(name: 'AWS_REGION', defaultValue: 'us-east-1', description: 'AWS region to deploy into')
    string(name: 'ECR_REPOSITORY', defaultValue: 'account-service', description: 'ECR repository name')
    string(name: 'ECS_CLUSTER', defaultValue: 'account-service-cluster', description: 'ECS cluster name')
    string(name: 'ECS_SERVICE', defaultValue: 'account-service', description: 'ECS service name')
    string(name: 'DOCKER_IMAGE_TAG', defaultValue: "${env.BUILD_NUMBER}", description: 'Docker image tag')
    string(name: 'SPRING_PROFILE', defaultValue: 'postgres', description: 'Spring profile to use at runtime')
  }

  environment {
    APP_NAME = 'account-service'
    AWS_DEFAULT_REGION = "${params.AWS_REGION}"
    ECR_URI = "${params.AWS_ACCOUNT_ID}.dkr.ecr.${params.AWS_REGION}.amazonaws.com/${params.ECR_REPOSITORY}"
    IMAGE = "${ECR_URI}:${params.DOCKER_IMAGE_TAG}"
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

    stage('Test') {
      steps {
        sh 'mvn test'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "docker build -t ${IMAGE} ."
      }
    }

    stage('Login to ECR') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
          sh '''#!/bin/bash
          aws configure set region ${AWS_DEFAULT_REGION}
          aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${ECR_URI}
          '''
        }
      }
    }

    stage('Push Image to ECR') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
          sh '''#!/bin/bash
          aws ecr describe-repositories --repository-names ${params.ECR_REPOSITORY} >/dev/null 2>&1 || \
            aws ecr create-repository --repository-name ${params.ECR_REPOSITORY} --region ${AWS_DEFAULT_REGION}
          docker push ${IMAGE}
          '''
        }
      }
    }

    stage('Deploy to ECS') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
          sh '''#!/bin/bash
          aws ecs update-service --cluster ${params.ECS_CLUSTER} --service ${params.ECS_SERVICE} --force-new-deployment --region ${AWS_DEFAULT_REGION}
          '''
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
    }
    success {
      echo "Deployment completed: ${IMAGE}"
    }
    failure {
      echo 'Pipeline failed. Check console output for details.'
    }
  }
}
