package crm.utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Cloud-ready utility for reading data from Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class ReadDataUtils {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private final S3Client s3Client;

    public ReadDataUtils(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Downloads a file from S3 and returns it as a temporary local file.
     * This maintains backward compatibility with code expecting File objects.
     * 
     * @param s3Key The S3 object key (path within the bucket)
     * @return File object pointing to the downloaded temporary file
     * @throws IOException if download fails
     */
    public File readFileFromS3(String s3Key) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(S3_BUCKET_NAME)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            // Create a temporary file
            String fileName = s3Key.substring(s3Key.lastIndexOf('/') + 1);
            File tempFile = File.createTempFile("s3-download-", "-" + fileName);
            tempFile.deleteOnExit();

            // Write S3 content to temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream is = s3Object) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;
        } catch (S3Exception e) {
            throw new IOException("Failed to read file from S3: " + s3Key, e);
        }
    }

    /**
     * Downloads a file from S3 and returns it as an InputStream.
     * This is more efficient for cloud environments as it avoids local file system writes.
     * 
     * @param s3Key The S3 object key (path within the bucket)
     * @return InputStream containing the S3 object data
     * @throws IOException if download fails
     */
    public InputStream readStreamFromS3(String s3Key) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(S3_BUCKET_NAME)
                    .key(s3Key)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (S3Exception e) {
            throw new IOException("Failed to read stream from S3: " + s3Key, e);
        }
    }
}
