package crm.csv;

import com.opencsv.CSVReader;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready CSV processing utility that reads from Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class CSVTest {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-data-bucket");
    private static final String AWS_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

    public static void main(String[] args) {
        // Example S3 key - in production, this would be passed as a parameter or configuration
        String s3Key = System.getenv().getOrDefault("CSV_S3_KEY", "data/sample.csv");
        
        processCSVFromS3(s3Key);
    }

    /**
     * Processes a CSV file from Amazon S3 instead of local file system.
     * This ensures the application works in cloud and containerized environments.
     * 
     * @param s3Key The S3 object key (path within the bucket)
     */
    public static void processCSVFromS3(String s3Key) {
        // Initialize S3 client with default credentials provider (uses IAM roles in cloud)
        try (S3Client s3Client = S3Client.builder()
                .region(Region.of(AWS_REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(S3_BUCKET_NAME)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            CSVReader reader = new CSVReader(new InputStreamReader(s3Object));
            List<Object[]> data = new ArrayList<>();
            
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                }
            }
            
            reader.close();
            System.out.println("Successfully processed CSV from S3: s3://" + S3_BUCKET_NAME + "/" + s3Key);
            System.out.println("Total rows processed: " + data.size());
            
        } catch (IOException e) {
            System.err.println("Failed to process CSV from S3: " + s3Key);
            e.printStackTrace();
        }
    }

    /**
     * Alternative method that returns the parsed CSV data for further processing.
     * 
     * @param s3Key The S3 object key (path within the bucket)
     * @return List of string arrays representing CSV rows
     * @throws IOException if S3 read or CSV parsing fails
     */
    public static List<String[]> readCSVFromS3(String s3Key) throws IOException {
        try (S3Client s3Client = S3Client.builder()
                .region(Region.of(AWS_REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(S3_BUCKET_NAME)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            CSVReader reader = new CSVReader(new InputStreamReader(s3Object));
            
            List<String[]> data = new ArrayList<>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
            }
            
            reader.close();
            return data;
        }
    }
}
