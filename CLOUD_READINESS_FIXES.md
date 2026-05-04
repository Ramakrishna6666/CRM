# Cloud Readiness Fixes - AWS Migration

## Overview
This document describes the cloud readiness fixes applied to make the CRM application fully compatible with AWS cloud deployment. All fixes follow 12-factor app principles and cloud-native patterns.

## Fixed Issues

### 1. Hard-coded File Paths (cr-java-0061)
**File:** `crm/utils/ReadDataUtils.java`
**Severity:** Critical
**Fix Applied:** Replaced hard-coded file paths with Amazon S3 object storage

**Changes:**
- Removed dependency on local file system and Swing JFileChooser
- Implemented S3-based file reading using AWS SDK for Java v2
- Added methods to read files from S3 as File objects or InputStreams
- Uses environment variable `S3_BUCKET_NAME` for bucket configuration
- Supports IAM role-based authentication for cloud deployments

**Configuration:**
```properties
aws.s3.bucket.name=${S3_BUCKET_NAME:crm-data-bucket}
aws.region=${AWS_REGION:us-east-1}
```

### 2. Local File System Write Operations (cr-java-0062)
**File:** `crm/controller/PdfController.java`
**Severity:** Critical
**Fix Applied:** Replaced local file writes with Amazon S3 for durable storage

**Changes:**
- Replaced `FileOutputStream` with in-memory `ByteArrayOutputStream`
- PDF generation now happens in memory instead of writing to local disk
- PDFs are uploaded directly to S3 using `S3Client.putObject()`
- Added proper error handling and logging for S3 operations
- Updated Pdf entity to store S3 key instead of local file path

**Configuration:**
```properties
aws.s3.pdf.bucket.name=${S3_PDF_BUCKET_NAME:crm-pdf-bucket}
aws.s3.pdf.prefix=${S3_PDF_PREFIX:pdfs/}
```

### 3. Java.io.File Usage for Data Storage (cr-java-0063)
**File:** `crm/csv/CSVTest.java`
**Severity:** Critical
**Fix Applied:** Migrated java.io.File operations to Amazon S3 using AWS SDK for Java v2

**Changes:**
- Removed dependency on local file system and Swing file chooser
- Implemented S3-based CSV reading using AWS SDK for Java v2
- CSV files are now read directly from S3 as InputStreams
- Added `processCSVFromS3()` method for S3-based CSV processing
- Added `readCSVFromS3()` method that returns parsed data
- Uses DefaultCredentialsProvider for IAM role support

**Configuration:**
```properties
aws.s3.bucket.name=${S3_BUCKET_NAME:crm-data-bucket}
CSV_S3_KEY=${CSV_S3_KEY:data/sample.csv}
```

### 4. Clock/Time Dependencies (cr-java-0111)
**File:** `crm/controller/DateTimeTestController.java`
**Severity:** High
**Fix Applied:** Replaced java.util.Date/Timer with java.time API and standardized on UTC

**Changes:**
- Removed `java.util.Date` usage (lines 19-20)
- Migrated to `java.time` API: `Instant`, `ZonedDateTime`, `LocalDateTime`, `Clock`
- Standardized all time operations on UTC timezone
- Added `Clock` abstraction for testability
- All timestamps now use `Instant.now(clock)` for consistency
- Added helper methods for timezone conversions
- Configured Hibernate and Jackson to use UTC

**Configuration:**
```properties
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jackson.time-zone=UTC
```

## New Files Created

### 1. AWS S3 Configuration
**File:** `crm/config/AwsS3Config.java`
**Purpose:** Spring configuration for S3Client bean

**Features:**
- Creates S3Client bean for dependency injection
- Uses DefaultCredentialsProvider (supports IAM roles)
- Configurable AWS region via properties
- Automatic credential resolution for cloud environments

## Dependencies Added

### AWS SDK for Java v2
Added to `pom.xml`:
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.17.100</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>auth</artifactId>
    <version>2.17.100</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>regions</artifactId>
    <version>2.17.100</version>
</dependency>
```

## Environment Variables

The following environment variables should be configured in cloud deployments:

### Required
- `AWS_REGION` - AWS region (default: us-east-1)
- `S3_BUCKET_NAME` - S3 bucket for general data storage (default: crm-data-bucket)
- `S3_PDF_BUCKET_NAME` - S3 bucket for PDF storage (default: crm-pdf-bucket)

### Optional
- `S3_PDF_PREFIX` - Prefix for PDF objects in S3 (default: pdfs/)
- `CSV_S3_KEY` - S3 key for CSV file processing (default: data/sample.csv)

### AWS Credentials (handled automatically via IAM roles)
When running in AWS (EC2, ECS, Lambda), credentials are automatically provided via IAM roles.
For local development, use:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
Or configure `~/.aws/credentials`

## Cloud Deployment Checklist

### Pre-deployment
- [ ] Create S3 buckets: `crm-data-bucket` and `crm-pdf-bucket`
- [ ] Configure IAM role with S3 permissions (GetObject, PutObject, ListBucket)
- [ ] Set environment variables in cloud platform
- [ ] Verify AWS region configuration
- [ ] Upload any existing CSV/data files to S3

### IAM Policy Example
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::crm-data-bucket/*",
        "arn:aws:s3:::crm-pdf-bucket/*",
        "arn:aws:s3:::crm-data-bucket",
        "arn:aws:s3:::crm-pdf-bucket"
      ]
    }
  ]
}
```

### Post-deployment
- [ ] Verify S3 connectivity from application
- [ ] Test PDF generation and upload to S3
- [ ] Test CSV reading from S3
- [ ] Verify UTC timezone handling
- [ ] Monitor CloudWatch logs for any S3 errors

## Benefits of Cloud-Ready Changes

1. **Scalability**: S3 provides unlimited storage without managing disk space
2. **Durability**: S3 offers 99.999999999% (11 9's) durability
3. **Availability**: Data accessible from any container/instance
4. **Cost-effective**: Pay only for storage used, no provisioning required
5. **Stateless**: Application instances are now stateless and can scale horizontally
6. **Time consistency**: UTC standardization eliminates timezone issues in distributed systems
7. **Security**: IAM role-based authentication eliminates hardcoded credentials

## Testing

### Local Testing
For local development, configure AWS credentials:
```bash
export AWS_ACCESS_KEY_ID=your_key
export AWS_SECRET_ACCESS_KEY=your_secret
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=your-test-bucket
export S3_PDF_BUCKET_NAME=your-pdf-bucket
```

### Integration Testing
1. Upload a test CSV file to S3
2. Run CSVTest with CSV_S3_KEY pointing to the test file
3. Generate a PDF and verify it appears in S3
4. Check date/time endpoints return UTC timestamps

## Migration Notes

### Data Migration
If you have existing local files:
1. Upload existing PDFs to S3 under the `pdfs/` prefix
2. Upload CSV files to S3 under appropriate keys
3. Update database records with S3 keys if needed

### Backward Compatibility
The changes maintain API compatibility where possible:
- ReadDataUtils now returns File objects (downloaded from S3)
- PdfController endpoints remain unchanged
- DateTimeTestController endpoints remain unchanged

## Troubleshooting

### S3 Access Denied
- Verify IAM role has correct permissions
- Check bucket names are correct
- Verify AWS region matches bucket region

### Connection Timeout
- Check security groups allow outbound HTTPS (port 443)
- Verify VPC has internet gateway or S3 VPC endpoint

### Timezone Issues
- Verify `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` is set
- Check database timezone configuration
- Ensure all date/time operations use java.time API

## Support

For issues or questions about these cloud readiness fixes, refer to:
- AWS SDK for Java v2 documentation: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/
- Spring Boot on AWS: https://spring.io/guides/gs/spring-boot-aws/
- Java Time API: https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html
