# CRM Application - Cloud Readiness Fixes

## Overview
This application has been updated to be cloud-ready for AWS deployment. All local file system dependencies have been replaced with Amazon S3 object storage, and time-related code has been migrated to use the java.time API with UTC standardization.

## Cloud Readiness Changes

### 1. File Storage Migration to Amazon S3
**Files Modified:**
- `ReadDataUtils.java` - Replaced JFileChooser with S3 client for reading files
- `PdfController.java` - Replaced local file writes with S3 uploads
- `CSVTest.java` - Replaced java.io.File operations with S3 reads
- `Pdf.java` - Added s3Key field to store S3 object location

**Benefits:**
- Data durability and availability across regions
- No dependency on ephemeral container file systems
- Scalable storage without host-level constraints
- Automatic backup and versioning capabilities

### 2. Time/Clock Dependencies Fixed
**Files Modified:**
- `DateTimeTestController.java` - Migrated from java.util.Date to java.time API with UTC standardization

**Benefits:**
- Consistent timezone handling across distributed systems
- No dependency on server-local timezone settings
- Testable with Clock injection
- ISO-8601 compliant timestamps

### 3. AWS SDK Integration
**Files Added:**
- `AwsS3Config.java` - Spring configuration for S3Client bean

**Dependencies Added:**
- AWS SDK for Java v2 (S3 client)
- AWS SDK Core

## Environment Variables Required

### AWS Configuration
```bash
# S3 Bucket for PDF and file storage
AWS_S3_BUCKET_NAME=your-crm-bucket-name

# AWS Region
AWS_REGION=us-east-1

# AWS Credentials (automatically provided by IAM roles in ECS/EKS)
# For local development only:
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

### Database Configuration
```bash
# Replace hardcoded database connection with environment variables
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-endpoint:3306/crm?useSSL=true
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password
```

## AWS Services Required

### 1. Amazon S3
- Create an S3 bucket for PDF and file storage
- Configure bucket policies for application access
- Enable versioning for data protection
- Configure lifecycle policies for cost optimization

### 2. Amazon RDS (MySQL)
- Replace localhost MySQL with RDS instance
- Configure security groups for application access
- Enable automated backups
- Use parameter groups for MySQL optimization

### 3. IAM Roles and Policies
For ECS/EKS deployment, create IAM role with policies:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-crm-bucket-name/*",
        "arn:aws:s3:::your-crm-bucket-name"
      ]
    }
  ]
}
```

## Local Development Setup

### 1. Install AWS CLI
```bash
# Install AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configure credentials
aws configure
```

### 2. Create S3 Bucket
```bash
aws s3 mb s3://crm-dev-bucket --region us-east-1
```

### 3. Set Environment Variables
```bash
export AWS_S3_BUCKET_NAME=crm-dev-bucket
export AWS_REGION=us-east-1
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/crm?useSSL=false
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=password
```

### 4. Run Application
```bash
mvn spring-boot:run
```

## Testing Cloud Features

### Test PDF Generation with S3 Upload
1. Navigate to `/pdf-generator`
2. Enter PDF name and content
3. Submit form
4. Verify PDF is uploaded to S3:
```bash
aws s3 ls s3://your-bucket-name/pdfs/
```

### Test CSV Processing from S3
1. Upload a CSV file to S3:
```bash
aws s3 cp test.csv s3://your-bucket-name/csv/test.csv
```

2. Run CSVTest with environment variables:
```bash
AWS_S3_BUCKET_NAME=your-bucket-name AWS_S3_CSV_KEY=csv/test.csv java crm.csv.CSVTest
```

### Test Date/Time with UTC
1. Navigate to `/date/test`
2. Verify all timestamps are in UTC
3. Check ISO-8601 formatted timestamps

## Deployment Considerations

### Container Deployment (ECS/EKS)
- Use IAM roles for service accounts (no hardcoded credentials)
- Mount secrets from AWS Secrets Manager for database credentials
- Configure health checks on `/appinfo/health`
- Set resource limits (CPU/Memory)

### Environment-Specific Configuration
- Use AWS Systems Manager Parameter Store for configuration
- Override application.properties with environment variables
- Use different S3 buckets per environment (dev/staging/prod)

### Monitoring and Logging
- Enable CloudWatch Logs for application logs
- Configure CloudWatch metrics for S3 operations
- Set up alarms for S3 errors and latency
- Monitor RDS performance metrics

## Migration Checklist

- [x] Replace local file operations with S3
- [x] Migrate java.util.Date to java.time API
- [x] Add AWS SDK dependencies
- [x] Create S3Client configuration bean
- [x] Update entity models for S3 keys
- [x] Add environment variable configuration
- [ ] Create S3 bucket in AWS
- [ ] Configure IAM roles and policies
- [ ] Set up RDS database
- [ ] Configure CloudWatch monitoring
- [ ] Test in AWS environment
- [ ] Update CI/CD pipeline for cloud deployment

## Troubleshooting

### S3 Access Denied
- Verify IAM role has correct S3 permissions
- Check bucket policy allows application access
- Ensure bucket name is correct in environment variables

### Database Connection Failed
- Verify RDS security group allows inbound traffic
- Check database credentials in environment variables
- Ensure RDS endpoint is correct

### PDF Upload Failed
- Check S3 bucket exists and is accessible
- Verify AWS credentials are configured
- Check CloudWatch Logs for detailed error messages

## Additional Resources
- [AWS SDK for Java v2 Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Amazon S3 Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/best-practices.html)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-aws/)
