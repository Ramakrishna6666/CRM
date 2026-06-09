package crm.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

/**
 * Cloud-native utility for reading data from Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class ReadDataUtils {

    private final S3Client s3Client;
    private final String bucketName;

    /**
     * Constructor with S3 client and bucket name.
     * These should be injected via Spring configuration.
     * 
     * @param s3Client AWS S3 client instance
     * @param bucketName S3 bucket name from environment variable
     */
    public ReadDataUtils(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * Read file from S3 bucket.
     * 
     * @param objectKey S3 object key (file path in bucket)
     * @return InputStream of the S3 object
     * @throws IOException if file cannot be read from S3
     */
    public InputStream readFileFromS3(String objectKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return s3Object;
        } catch (S3Exception e) {
            throw new IOException("Failed to read file from S3: " + objectKey, e);
        }
    }

    /**
     * Read file from S3 with custom bucket.
     * 
     * @param bucketName Custom bucket name
     * @param objectKey S3 object key
     * @return InputStream of the S3 object
     * @throws IOException if file cannot be read from S3
     */
    public InputStream readFileFromS3(String bucketName, String objectKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return s3Object;
        } catch (S3Exception e) {
            throw new IOException("Failed to read file from S3 bucket " + bucketName + ": " + objectKey, e);
        }
    }

}
