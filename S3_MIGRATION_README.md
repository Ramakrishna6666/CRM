# Cloud Readiness Migration - S3 File Storage

## Overview
This application has been migrated from local file system dependencies to Amazon S3 object storage for cloud compatibility.

## Changes Made

### 1. Hard-coded File Paths Removed (Rule: cr-java-0061)
- **File Modified**: `src/main/java/crm/utils/ReadDataUtils.java`
- **Original Issue**: Used `JFileChooser` (Swing GUI component) to select files from local file system
- **Fix Applied**: Replaced with AWS SDK for Java v2 S3 client to download files from S3 buckets

### 2. Dependencies Added
- AWS SDK for Java v2 (BOM version 2.17.290)
- `software.amazon.awssdk:s3` - S3 client library
- `software.amazon.awssdk:auth` - Authentication support

## Configuration Required

### Environment Variables
Set the following environment variables in your cloud environment:

```bash
# Required
S3_BUCKET_NAME=your-crm-data-bucket
AWS_REGION=us-east-1

# Optional (if not using IAM roles)
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

### AWS IAM Permissions
The application requires the following S3 permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-crm-data-bucket",
        "arn:aws:s3:::your-crm-data-bucket/*"
      ]
    }
  ]
}
```

## Usage Changes

### Before (Local File System)
```java
File document = ReadDataUtils.ReadFile("Select CSV file", null, "Only CSV Files", "csv");
```

### After (S3 Object Storage)
```java
// Option 1: Using S3 key directly
File document = ReadDataUtils.ReadFile("data/customers.csv", "csv");

// Option 2: With custom bucket
File document = ReadDataUtils.ReadFile("data/customers.csv", "my-custom-bucket", "CSV Files", "csv");

// Option 3: Stream directly (more efficient)
InputStream stream = ReadDataUtils.getFileAsStream("data/customers.csv", null);
```

## Migration Steps for Data

1. **Upload existing CSV files to S3**:
   ```bash
   aws s3 cp local-data/ s3://your-crm-data-bucket/data/ --recursive
   ```

2. **Update code references**:
   - Replace file dialog calls with S3 key paths
   - Update file paths to use S3 object keys (e.g., "data/customers.csv")

3. **Test in cloud environment**:
   - Ensure IAM roles are properly configured
   - Verify S3 bucket access
   - Test file download functionality

## Benefits of S3 Migration

✅ **Cloud-Native**: Works seamlessly in containerized environments (ECS, EKS, Lambda)
✅ **Scalable**: No local disk space limitations
✅ **Durable**: 99.999999999% (11 9's) durability
✅ **Secure**: Integrated with AWS IAM for access control
✅ **Cost-Effective**: Pay only for storage used
✅ **No GUI Required**: Works in headless server environments

## Backward Compatibility

The `ReadDataUtils.ReadFile()` method signature has been maintained for backward compatibility, but the parameters now have different meanings:
- `dialogMessage` → `s3Key` (S3 object key/path)
- `parent` → `bucketName` (optional S3 bucket name)
- `fileExtensionDescription` → Used for logging only
- `fileExtension` → Still validates file extensions

## Troubleshooting

### Issue: "Access Denied" errors
**Solution**: Verify IAM role/user has `s3:GetObject` permission for the bucket

### Issue: "Bucket does not exist"
**Solution**: Ensure `S3_BUCKET_NAME` environment variable is set correctly and bucket exists

### Issue: "Region not found"
**Solution**: Set `AWS_REGION` environment variable to the correct AWS region

### Issue: Files not found
**Solution**: Verify S3 object keys are correct (case-sensitive, include full path)

## Additional Resources
- [AWS SDK for Java v2 Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Amazon S3 Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/best-practices.html)
- [IAM Roles for EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/iam-roles-for-amazon-ec2.html)
