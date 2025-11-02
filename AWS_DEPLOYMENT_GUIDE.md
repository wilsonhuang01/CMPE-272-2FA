# AWS Deployment Guide

This guide provides step-by-step instructions for deploying the CMPE-272 2FA application on AWS using ECS Fargate, Application Load Balancers, and RDS MySQL.

## Architecture Overview

The deployment uses the following AWS services:

- **ECS Fargate**: Container orchestration for frontend and backend services
- **ECR**: Docker image registry
- **Application Load Balancers (ALB)**: Traffic distribution and SSL termination
- **RDS MySQL**: Managed database service
- **Route 53**: DNS management (optional)
- **CloudWatch**: Logging and monitoring
- **VPC**: Network isolation and security

## Prerequisites

- AWS Account with appropriate permissions
- AWS CLI installed and configured (`aws configure`)
- Docker installed locally (for building images)
- Domain name (optional, for custom domain)
- Basic understanding of AWS services

## Deployment Steps Overview

1. **Infrastructure Setup**: VPC, subnets, security groups
2. **Database Setup**: RDS MySQL instance and database creation
3. **Docker Images**: Build and push to ECR
4. **ECS Setup**: Cluster, task definitions, and services
5. **Load Balancers**: ALBs and target groups
6. **DNS Configuration**: Route 53 (optional)

## Step 1: Infrastructure Setup

### 1.1 VPC and Networking

**Requirements:**
- VPC with public subnets for ALBs and ECS tasks
- Internet Gateway attached to VPC
- Route tables configured with routes to Internet Gateway (0.0.0.0/0 → IGW)
- All subnets used by ECS tasks must have internet access for ECR image pulls

**Verification:**
```bash
# Check route tables have internet gateway routes
aws ec2 describe-route-tables \
    --filters "Name=association.subnet-id,Values=<your-subnet-id>" \
    --query 'RouteTables[*].Routes[?DestinationCidrBlock==`0.0.0.0/0`]'
```

### 1.2 Security Groups

Create security groups with the following rules:

**Backend Security Group:**
- Inbound: Port 8080 from Backend ALB security group
- Outbound: All traffic

**Frontend Security Group:**
- Inbound: Port 80 from Frontend ALB security group
- Outbound: All traffic

**RDS Security Group:**
- Inbound: Port 3306 from Backend security group ONLY
- Outbound: None

**Backend ALB Security Group:**
- Inbound: Port 80/443 from 0.0.0.0/0
- Outbound: All traffic

**Frontend ALB Security Group:**
- Inbound: Port 80/443 from 0.0.0.0/0
- Outbound: All traffic

### 1.3 RDS Database

**Create DB Subnet Group:**
```bash
aws rds create-db-subnet-group \
    --db-subnet-group-name cmpe272-db-subnet \
    --db-subnet-group-description "DB subnet group for application" \
    --subnet-ids <subnet-id-1> <subnet-id-2> \
    --region <your-region>
```

**Create RDS Security Group:**
```bash
aws ec2 create-security-group \
    --group-name cmpe272-rds-sg \
    --description "Security group for RDS MySQL" \
    --vpc-id <your-vpc-id> \
    --region <your-region>
```

**Add Inbound Rule:**
```bash
aws ec2 authorize-security-group-ingress \
    --group-id <rds-security-group-id> \
    --protocol tcp \
    --port 3306 \
    --source-group <backend-security-group-id> \
    --region <your-region>
```

**Create RDS Instance:**
```bash
aws rds create-db-instance \
    --db-instance-identifier cmpe272-mysql \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --engine-version 8.0 \
    --master-username cmpe272_user \
    --master-user-password <secure-password> \
    --allocated-storage 20 \
    --vpc-security-group-ids <rds-security-group-id> \
    --db-subnet-group-name cmpe272-db-subnet \
    --backup-retention-period 7 \
    --storage-encrypted \
    --no-publicly-accessible \
    --region <your-region>
```

**⚠️ CRITICAL: Create Database Before Deployment**

The RDS instance creation doesn't create the database itself. You must create the `cmpe272` database separately:

```bash
# Option 1: Using ECS task (Recommended)
# Create a one-time ECS task with mysql:8.0 image

# Option 2: Using MySQL client locally (if RDS is publicly accessible)
mysql -h <rds-endpoint> \
    -u cmpe272_user \
    -p <password> \
    -e "CREATE DATABASE IF NOT EXISTS cmpe272 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

## Step 2: Docker Images

### 2.1 ECR Setup

**Create ECR Repositories:**
```bash
aws ecr create-repository --repository-name cmpe272-backend --region <region>
aws ecr create-repository --repository-name cmpe272-frontend --region <region>
```

**Login to ECR:**
```bash
aws ecr get-login-password --region <region> | \
    docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com
```

### 2.2 Build and Push Backend

**Important:** Use `--platform linux/amd64` for Fargate compatibility (especially if building on Apple Silicon):

```bash
cd backend
docker build --platform linux/amd64 -t cmpe272-backend .
docker tag cmpe272-backend:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cmpe272-backend:latest
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/cmpe272-backend:latest
```

### 2.3 Build and Push Frontend

**Get Backend ALB DNS Name:**
```bash
# After creating backend ALB (Step 4), get its DNS name
BACKEND_ALB_DNS=$(aws elbv2 describe-load-balancers \
    --region <region> \
    --query 'LoadBalancers[?LoadBalancerName==`cmpe272-backend-alb`].DNSName' \
    --output text)
```

**Build Frontend:**
```bash
cd frontend
docker build \
    --platform linux/amd64 \
    --build-arg REACT_APP_API_BASE_URL=http://$BACKEND_ALB_DNS/api/auth \
    -t cmpe272-frontend .

docker tag cmpe272-frontend:latest <account-id>.dkr.ecr.<region>.amazonaws.com/cmpe272-frontend:latest
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/cmpe272-frontend:latest
```

**Note:** For custom domain, use `https://api.yourdomain.com/api/auth` instead.

## Step 3: ECS Setup

### 3.1 Task Execution Role

Fargate requires an execution role for pulling images and writing logs:

```bash
# Create role
aws iam create-role \
    --role-name ecsTaskExecutionRole \
    --assume-role-policy-document '{
      "Version": "2012-10-17",
      "Statement": [{
        "Effect": "Allow",
        "Principal": {"Service": "ecs-tasks.amazonaws.com"},
        "Action": "sts:AssumeRole"
      }]
    }'

# Attach policy
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Get role ARN
EXECUTION_ROLE_ARN=$(aws iam get-role --role-name ecsTaskExecutionRole --query 'Role.Arn' --output text)
```

### 3.2 ECS Cluster

```bash
aws ecs create-cluster \
    --cluster-name cmpe272-cluster \
    --capacity-providers FARGATE FARGATE_SPOT \
    --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1 \
    --region <region>
```

### 3.3 CloudWatch Log Groups

```bash
aws logs create-log-group --log-group-name /ecs/cmpe272-backend --region <region>
aws logs create-log-group --log-group-name /ecs/cmpe272-frontend --region <region>
```

### 3.4 Task Definitions

**Backend Task Definition:**

Create `backend-task-definition.json`:
```json
{
  "family": "cmpe272-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "executionRoleArn": "<execution-role-arn>",
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [{
    "name": "backend",
    "image": "<account-id>.dkr.ecr.<region>.amazonaws.com/cmpe272-backend:latest",
    "portMappings": [{"containerPort": 8080, "protocol": "tcp"}],
    "environment": [
      {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
      {"name": "DB_HOST", "value": "<rds-endpoint>"},
      {"name": "DB_PORT", "value": "3306"},
      {"name": "DB_NAME", "value": "cmpe272"},
      {"name": "DB_USERNAME", "value": "cmpe272_user"},
      {"name": "DB_PASSWORD", "value": "<secure-password>"},
      {"name": "JWT_SECRET", "value": "<secure-random-secret>"},
      {"name": "MAIL_HOST", "value": "smtp.gmail.com"},
      {"name": "MAIL_PORT", "value": "587"},
      {"name": "MAIL_USERNAME", "value": "<your-email>"},
      {"name": "MAIL_PASSWORD", "value": "<gmail-app-password>"},
      {"name": "ALLOWED_ORIGINS", "value": "<frontend-alb-dns-or-domain>"}
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/cmpe272-backend",
        "awslogs-region": "<region>",
        "awslogs-stream-prefix": "ecs"
      }
    },
    "healthCheck": {
      "command": ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"],
      "interval": 30,
      "timeout": 5,
      "retries": 3,
      "startPeriod": 60
    }
  }]
}
```

**Frontend Task Definition:**

Create `frontend-task-definition.json`:
```json
{
  "family": "cmpe272-frontend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "executionRoleArn": "<execution-role-arn>",
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [{
    "name": "frontend",
    "image": "<account-id>.dkr.ecr.<region>.amazonaws.com/cmpe272-frontend:latest",
    "portMappings": [{"containerPort": 80, "protocol": "tcp"}],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/cmpe272-frontend",
        "awslogs-region": "<region>",
        "awslogs-stream-prefix": "ecs"
      }
    }
  }]
}
```

**Register Task Definitions:**
```bash
aws ecs register-task-definition --cli-input-json file://backend-task-definition.json --region <region>
aws ecs register-task-definition --cli-input-json file://frontend-task-definition.json --region <region>
```

## Step 4: Application Load Balancers

### 4.1 Create ALBs

**Backend ALB:**
```bash
aws elbv2 create-load-balancer \
    --name cmpe272-backend-alb \
    --subnets <subnet-id-1> <subnet-id-2> \
    --security-groups <backend-alb-security-group-id> \
    --scheme internet-facing \
    --type application \
    --region <region>
```

**Frontend ALB:**
```bash
aws elbv2 create-load-balancer \
    --name cmpe272-frontend-alb \
    --subnets <subnet-id-1> <subnet-id-2> \
    --security-groups <frontend-alb-security-group-id> \
    --scheme internet-facing \
    --type application \
    --region <region>
```

### 4.2 Create Target Groups

**Backend Target Group:**
```bash
aws elbv2 create-target-group \
    --name cmpe272-backend-tg \
    --protocol HTTP \
    --port 8080 \
    --vpc-id <vpc-id> \
    --target-type ip \
    --health-check-path /actuator/health \
    --health-check-interval-seconds 30 \
    --region <region>
```

**Frontend Target Group:**
```bash
aws elbv2 create-target-group \
    --name cmpe272-frontend-tg \
    --protocol HTTP \
    --port 80 \
    --vpc-id <vpc-id> \
    --target-type ip \
    --health-check-path /health \
    --region <region>
```

### 4.3 Create Listeners

**Backend Listener:**
```bash
aws elbv2 create-listener \
    --load-balancer-arn <backend-alb-arn> \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=<backend-target-group-arn> \
    --region <region>
```

**Frontend Listener:**
```bash
aws elbv2 create-listener \
    --load-balancer-arn <frontend-alb-arn> \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=<frontend-target-group-arn> \
    --region <region>
```

**Note:** For HTTPS, create additional listeners on port 443 with SSL certificates from AWS Certificate Manager (ACM).

## Step 5: ECS Services

### 5.1 Create Backend Service

```bash
aws ecs create-service \
    --cluster cmpe272-cluster \
    --service-name cmpe272-backend \
    --task-definition cmpe272-backend \
    --desired-count 2 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[<subnet-id-1>,<subnet-id-2>],securityGroups=[<backend-sg-id>],assignPublicIp=ENABLED}" \
    --load-balancers "targetGroupArn=<backend-tg-arn>,containerName=backend,containerPort=8080" \
    --health-check-grace-period-seconds 120 \
    --region <region>
```

### 5.2 Create Frontend Service

```bash
aws ecs create-service \
    --cluster cmpe272-cluster \
    --service-name cmpe272-frontend \
    --task-definition cmpe272-frontend \
    --desired-count 2 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[<subnet-id-1>,<subnet-id-2>],securityGroups=[<frontend-sg-id>],assignPublicIp=ENABLED}" \
    --load-balancers "targetGroupArn=<frontend-tg-arn>,containerName=frontend,containerPort=80" \
    --region <region>
```

## Step 6: DNS Configuration (Optional)

### 6.1 Route 53 Setup

If using a custom domain:

**Create Hosted Zone:**
```bash
aws route53 create-hosted-zone \
    --name yourdomain.com \
    --caller-reference $(date +%s)
```

**Create A Record for Frontend:**
```bash
aws route53 change-resource-record-sets \
    --hosted-zone-id <hosted-zone-id> \
    --change-batch '{
      "Changes": [{
        "Action": "CREATE",
        "ResourceRecordSet": {
          "Name": "yourdomain.com",
          "Type": "A",
          "AliasTarget": {
            "DNSName": "<frontend-alb-dns-name>",
            "HostedZoneId": "<alb-hosted-zone-id>",
            "EvaluateTargetHealth": true
          }
        }
      }]
    }'
```

**Create A Record for Backend API:**
```bash
aws route53 change-resource-record-sets \
    --hosted-zone-id <hosted-zone-id> \
    --change-batch '{
      "Changes": [{
        "Action": "CREATE",
        "ResourceRecordSet": {
          "Name": "api.yourdomain.com",
          "Type": "A",
          "AliasTarget": {
            "DNSName": "<backend-alb-dns-name>",
            "HostedZoneId": "<alb-hosted-zone-id>",
            "EvaluateTargetHealth": true
          }
        }
      }]
    }'
```

**Update Frontend Build:**
Rebuild frontend image with custom domain:
```bash
docker build \
    --platform linux/amd64 \
    --build-arg REACT_APP_API_BASE_URL=https://api.yourdomain.com/api/auth \
    -t cmpe272-frontend .
```

## Critical Requirements

### 1. Network Configuration

- **All subnets used by ECS tasks MUST have internet gateway routes** (0.0.0.0/0 → IGW)
- This is required for ECS tasks to pull Docker images from ECR
- Verify routes: `aws ec2 describe-route-tables --filters "Name=association.subnet-id,Values=<subnet-id>"`

### 2. Docker Architecture

- **Always use `--platform linux/amd64`** when building images
- Fargate requires `linux/amd64` architecture
- Error `exec format error` indicates wrong architecture

### 3. Database Setup

- **RDS instance creation ≠ Database creation**
- Create the `cmpe272` database separately before deploying backend
- Use ECS task with mysql:8.0 image if MySQL client unavailable locally

### 4. VPC Matching

- **RDS and ECS backend MUST be in the same VPC** for connectivity
- Verify: `aws rds describe-db-instances --query 'DBInstances[0].DBSubnetGroup.VpcId'`
- Verify: `aws ecs describe-services --query 'services[0].networkConfiguration'`

### 5. Security Groups

- RDS security group must allow inbound port 3306 from backend security group
- ECS task security groups must allow traffic from their respective ALB security groups
- ALB security groups must allow inbound 80/443 from 0.0.0.0/0

## Environment Variables Reference

### Backend Task Definition

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `DB_HOST` | RDS endpoint | `<rds-endpoint>.rds.amazonaws.com` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `cmpe272` |
| `DB_USERNAME` | Database username | `cmpe272_user` |
| `DB_PASSWORD` | Database password | `<secure-password>` |
| `JWT_SECRET` | JWT signing secret | `<secure-random-key>` |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | Email address | `<your-email@gmail.com>` |
| `MAIL_PASSWORD` | Gmail app password | `<app-password>` |
| `ALLOWED_ORIGINS` | CORS allowed origins | `<frontend-alb-dns>` or `https://yourdomain.com` |

### Frontend Build Arguments

| Variable | Description | Example |
|----------|-------------|---------|
| `REACT_APP_API_BASE_URL` | Backend API URL | `http://<backend-alb-dns>/api/auth` or `https://api.yourdomain.com/api/auth` |

## Monitoring and Troubleshooting

### Check Service Status

```bash
aws ecs describe-services \
    --cluster cmpe272-cluster \
    --services cmpe272-backend cmpe272-frontend \
    --query 'services[*].[serviceName,runningCount,desiredCount]' \
    --output table
```

### View Logs

```bash
# Backend logs
aws logs tail /ecs/cmpe272-backend --follow --region <region>

# Frontend logs
aws logs tail /ecs/cmpe272-frontend --follow --region <region>
```

### Check Target Health

```bash
# Backend target health
aws elbv2 describe-target-health \
    --target-group-arn <backend-tg-arn> \
    --region <region>

# Frontend target health
aws elbv2 describe-target-health \
    --target-group-arn <frontend-tg-arn> \
    --region <region>
```

### Common Issues

1. **Tasks failing to start**
   - Check CloudWatch logs for errors
   - Verify environment variables
   - Check security group rules
   - Verify task execution role permissions

2. **Database connection failures**
   - Verify RDS and ECS are in same VPC
   - Check security group rules (backend SG → RDS SG)
   - Verify database exists (not just RDS instance)
   - Check connection timeout settings

3. **CORS errors**
   - Verify `ALLOWED_ORIGINS` matches frontend URL
   - Check ALB health checks
   - Verify frontend API URL configuration

4. **503 Service Temporarily Unavailable**
   - Check target group health
   - Verify tasks are running
   - Check security group rules between ALB and tasks

5. **Exec format error**
   - Rebuild images with `--platform linux/amd64`

6. **ResourceInitializationError (cannot pull from ECR)**
   - Verify subnet route tables have internet gateway routes
   - Check task execution role permissions

## Cost Optimization

- Use Fargate Spot for non-critical workloads (up to 70% savings)
- Right-size containers (start with minimum resources)
- Use CloudFront for frontend static assets caching
- Enable RDS automated backups but set retention appropriately
- Set up auto-scaling based on CPU/Memory metrics

## Security Best Practices

1. **Secrets Management**: Use AWS Secrets Manager instead of environment variables for sensitive data
2. **SSL/TLS**: Use ACM certificates for HTTPS in production
3. **Network Isolation**: Place RDS in private subnets if possible
4. **IAM Roles**: Use least privilege principle for IAM roles
5. **VPC Security**: Use security groups to restrict traffic
6. **Database**: Enable encryption at rest and SSL connections

## Next Steps

1. Set up monitoring and CloudWatch alarms
2. Configure auto-scaling policies
3. Set up CI/CD pipeline (CodePipeline or GitHub Actions)
4. Configure SSL certificates for HTTPS
5. Set up backup and disaster recovery procedures
6. Implement rate limiting for API endpoints

---

**Last Updated:** November 2025

