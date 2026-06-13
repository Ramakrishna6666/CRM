package crm.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cloud-native utility for reading data from Amazon S3 instead of local file system.
 * Eliminates hard-coded file paths and file system dependencies.
 */
@Component
public class ReadDataUtils {

    @Value("${aws.s3.bucket.name:crm-data-bucket}")
    private String bucketName;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    private S3Client s3Client;

    public ReadDataUtils() {
        // Initialize S3 client with default credentials provider (uses IAM roles in cloud)
        this.s3Client = S3Client.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Read file content from S3 bucket by key
     * @param s3Key The S3 object key (path within bucket)
     * @return InputStream of the file content
     */
    public InputStream readFileFromS3(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return s3Object;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from S3: " + s3Key, e);
        }
    }

    /**
     * List files in S3 bucket with optional prefix filter
     * @param prefix Optional prefix to filter objects (e.g., "csv/", "data/")
     * @param fileExtension Optional file extension filter (e.g., "csv", "pdf")
     * @return List of S3 object keys matching the criteria
     */
    public List<String> listFilesFromS3(String prefix, String fileExtension) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName);

            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Request listRequest = requestBuilder.build();
            List<S3Object> objects = s3Client.listObjectsV2(listRequest).contents();

            return objects.stream()
                    .map(S3Object::key)
                    .filter(key -> fileExtension == null || key.endsWith("." + fileExtension))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files from S3", e);
        }
    }

    /**
     * Read file content as byte array from S3
     * @param s3Key The S3 object key
     * @return byte array of file content
     */
    public byte[] readFileAsBytesFromS3(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return s3Object.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file bytes from S3: " + s3Key, e);
        }
    }
}
