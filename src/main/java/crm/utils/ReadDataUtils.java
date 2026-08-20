package crm.utils;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Cloud-ready utility for reading files from Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class ReadDataUtils {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String AWS_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

    /**
     * Downloads a file from Amazon S3 and returns it as a File object.
     * 
     * @param s3Key The S3 object key (path) to download
     * @param fileExtension Expected file extension for validation
     * @return File object containing the downloaded content, or null if download fails
     */
    public static File ReadFile(String s3Key, String fileExtension) {
        return ReadFile(s3Key, null, null, fileExtension);
    }

    /**
     * Downloads a file from Amazon S3 and returns it as a File object.
     * This method maintains backward compatibility with the original signature.
     * 
     * @param s3Key The S3 object key (path) to download (replaces dialogMessage)
     * @param bucketName Optional bucket name (if null, uses environment variable S3_BUCKET_NAME)
     * @param fileExtensionDescription Description of file type (for logging purposes)
     * @param fileExtension Expected file extensions
     * @return File object containing the downloaded content, or null if download fails
     */
    public static File ReadFile(String s3Key, String bucketName, String fileExtensionDescription,
                                String... fileExtension) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            System.err.println("S3 key cannot be null or empty");
            return null;
        }

        String targetBucket = (bucketName != null && !bucketName.trim().isEmpty()) ? bucketName : S3_BUCKET_NAME;
        
        // Validate file extension if provided
        if (fileExtension != null && fileExtension.length > 0) {
            boolean validExtension = false;
            for (String ext : fileExtension) {
                if (s3Key.toLowerCase().endsWith("." + ext.toLowerCase())) {
                    validExtension = true;
                    break;
                }
            }
            if (!validExtension) {
                System.err.println("File does not have expected extension. Expected: " + String.join(", ", fileExtension));
                return null;
            }
        }

        try {
            // Create S3 client with default credentials provider (uses IAM roles, environment variables, or credentials file)
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            // Create GetObject request
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(s3Key)
                    .build();

            // Download the file from S3
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // Create a temporary file to store the downloaded content
            String fileName = s3Key.substring(s3Key.lastIndexOf('/') + 1);
            File tempFile = Files.createTempFile("s3-download-", "-" + fileName).toFile();
            tempFile.deleteOnExit(); // Clean up on JVM exit

            // Write S3 content to temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream is = s3Object) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("Successfully downloaded file from S3: " + s3Key + " to " + tempFile.getAbsolutePath());
            s3Client.close();
            return tempFile;

        } catch (S3Exception e) {
            System.err.println("S3 error while downloading file: " + e.awsErrorDetails().errorMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("IO error while downloading file from S3: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Downloads a file from S3 and returns it as an InputStream.
     * This is more efficient for streaming operations.
     * 
     * @param s3Key The S3 object key (path) to download
     * @param bucketName Optional bucket name (if null, uses environment variable S3_BUCKET_NAME)
     * @return InputStream of the S3 object, or null if download fails
     */
    public static InputStream getFileAsStream(String s3Key, String bucketName) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            System.err.println("S3 key cannot be null or empty");
            return null;
        }

        String targetBucket = (bucketName != null && !bucketName.trim().isEmpty()) ? bucketName : S3_BUCKET_NAME;

        try {
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(s3Key)
                    .build();

            return s3Client.getObject(getObjectRequest);

        } catch (S3Exception e) {
            System.err.println("S3 error while getting file stream: " + e.awsErrorDetails().errorMessage());
            e.printStackTrace();
            return null;
        }
    }
}
