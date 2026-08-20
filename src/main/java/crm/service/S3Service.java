package crm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.ByteArrayInputStream;

/**
 * Service class for Amazon S3 operations.
 * Provides cloud-ready file storage operations using S3.
 */
@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket:crm-data-bucket}")
     * Uploads a byte array to S3.
     * 
     * @param s3Key The S3 object key (path/filename)
     * @param data The byte array to upload
     * @param contentType The content type (e.g., "application/pdf")
     * @return true if upload successful, false otherwise
     */
    public boolean uploadFile(String s3Key, byte[] data, String contentType) {
        return uploadFile(s3Key, data, contentType, defaultBucketName);
    }

    /**
     * Uploads a byte array to S3.
     * 
     * @param s3Key The S3 object key (path/filename)
     * @param data The byte array to upload
     * @param contentType The content type (e.g., "application/pdf")
     * @param bucketName The S3 bucket name
     * @return true if upload successful, false otherwise
     */
    public boolean uploadFile(String s3Key, byte[] data, String contentType, String bucketName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
            return true;

        } catch (S3Exception e) {
            System.err.println("Error uploading file to S3: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Uploads an InputStream to S3.
     * 
     * @param s3Key The S3 object key (path/filename)
     * @param inputStream The input stream to upload
     * @param contentLength The content length in bytes
     * @param contentType The content type (e.g., "application/pdf")
     * @return true if upload successful, false otherwise
     */
    public boolean uploadFile(String s3Key, InputStream inputStream, long contentLength, String contentType) {
        return uploadFile(s3Key, inputStream, contentLength, contentType, defaultBucketName);
    }

    /**
     * Uploads an InputStream to S3.
     * 
     * @param s3Key The S3 object key (path/filename)
     * @param inputStream The input stream to upload
     * @param contentLength The content length in bytes
     * @param contentType The content type (e.g., "application/pdf")
     * @param bucketName The S3 bucket name
     * @return true if upload successful, false otherwise
     */
    public boolean uploadFile(String s3Key, InputStream inputStream, long contentLength, String contentType, String bucketName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            return true;

        } catch (S3Exception e) {
            System.err.println("Error uploading file to S3: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return File object, or null if download fails
     */
    public File downloadFile(String s3Key) {
        return downloadFile(s3Key, defaultBucketName);
    }

    /**
     * Downloads a file from S3 to a temporary local file.
     * 
     * @param s3Key The S3 object key
     * @param bucketName The S3 bucket name
     * @return File object, or null if download fails
     */
    public File downloadFile(String s3Key, String bucketName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            String fileName = s3Key.substring(s3Key.lastIndexOf('/') + 1);
            File tempFile = Files.createTempFile("s3-", "-" + fileName).toFile();
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream is = s3Object) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;

        } catch (S3Exception | IOException e) {
            System.err.println("Error downloading file from S3: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a file from S3 as an InputStream.
     * More efficient for streaming operations.
     * 
     * @param s3Key The S3 object key
     * @return InputStream of the S3 object
     */
    public InputStream getFileStream(String s3Key) {
        return getFileStream(s3Key, defaultBucketName);
    }

    /**
     * Gets a file from S3 as an InputStream.
     * 
     * @param s3Key The S3 object key
     * @param bucketName The S3 bucket name
     * @return InputStream of the S3 object
     */
    public InputStream getFileStream(String s3Key, String bucketName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            return s3Client.getObject(getObjectRequest);

        } catch (S3Exception e) {
            System.err.println("Error getting file stream from S3: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lists all objects in the default S3 bucket with a given prefix.
     * 
     * @param prefix The prefix to filter objects (e.g., "data/csv/")
     * @return List of S3 object keys
     */
    public List<String> listFiles(String prefix) {
        return listFiles(prefix, defaultBucketName);
    }

    /**
     * Lists all objects in an S3 bucket with a given prefix.
     * 
     * @param prefix The prefix to filter objects
     * @param bucketName The S3 bucket name
     * @return List of S3 object keys
     */
    public List<String> listFiles(String prefix, String bucketName) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            return listResponse.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            System.err.println("Error listing files from S3: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Checks if an object exists in S3.
     * 
     * @param s3Key The S3 object key
     * @return true if object exists, false otherwise
     */
    public boolean fileExists(String s3Key) {
        return fileExists(s3Key, defaultBucketName);
    }

    /**
     * Checks if an object exists in S3.
     * 
     * @param s3Key The S3 object key
     * @param bucketName The S3 bucket name
     * @return true if object exists, false otherwise
     */
    public boolean fileExists(String s3Key, String bucketName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.getObject(getObjectRequest).close();
            return true;

        } catch (S3Exception | IOException e) {
            return false;
        }
    }
}
