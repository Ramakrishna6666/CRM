package crm.csv;

import com.opencsv.CSVReader;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-native CSV processing using Amazon S3.
 * Replaces local file system dependencies with S3 object storage.
 */
public class CSVTest {

    /**
     * Read and process CSV file from Amazon S3.
     * 
     * @param s3Client AWS S3 client instance
     * @param bucketName S3 bucket name
     * @param objectKey S3 object key (CSV file path in bucket)
     * @return List of CSV rows as Object arrays
     * @throws IOException if S3 read fails
     */
    public static List<Object[]> readCsvFromS3(S3Client s3Client, String bucketName, String objectKey) throws IOException {
        List<Object[]> data = new ArrayList<>();
        
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            CSVReader reader = new CSVReader(new InputStreamReader(s3Object));
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                }
            }
            
            reader.close();
            s3Object.close();
            
        } catch (S3Exception e) {
            throw new IOException("Failed to read CSV from S3: bucket=" + bucketName + ", key=" + objectKey, e);
        }
        
        return data;
    }

    /**
     * Main method for testing CSV processing from S3.
     * Bucket name and object key should be provided via environment variables or command line arguments.
     * 
     * Example usage:
     * AWS_S3_BUCKET_NAME=my-bucket AWS_S3_CSV_KEY=data/file.csv java crm.csv.CSVTest
     */
    public static void main(String[] args) {
        // Get S3 configuration from environment variables
        String bucketName = System.getenv("AWS_S3_BUCKET_NAME");
        String objectKey = System.getenv("AWS_S3_CSV_KEY");
        
        if (bucketName == null || objectKey == null) {
            System.err.println("Error: AWS_S3_BUCKET_NAME and AWS_S3_CSV_KEY environment variables must be set");
            System.err.println("Example: AWS_S3_BUCKET_NAME=my-bucket AWS_S3_CSV_KEY=data/file.csv");
            System.exit(1);
        }
        
        // Create S3 client (uses default credential provider chain)
        S3Client s3Client = S3Client.builder().build();
        
        try {
            List<Object[]> data = readCsvFromS3(s3Client, bucketName, objectKey);
            System.out.println("Successfully processed " + data.size() + " rows from S3");
        } catch (IOException e) {
            System.err.println("Failed to process CSV from S3: " + e.getMessage());
            e.printStackTrace();
        } finally {
            s3Client.close();
        }
    }

}
