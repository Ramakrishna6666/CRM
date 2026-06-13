package crm.csv;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
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
 * Cloud-native CSV processing utility that reads CSV files from Amazon S3
 * instead of local file system, eliminating java.io.File dependencies.
 */
public class CSVTest {

    private static final String DEFAULT_BUCKET = System.getenv().getOrDefault("AWS_S3_BUCKET", "crm-data-bucket");
    private static final String DEFAULT_REGION = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

    /**
     * Read and process CSV file from Amazon S3
     * @param s3Key The S3 object key (path to CSV file in bucket)
     */
    public static void processCSVFromS3(String s3Key) {
        S3Client s3Client = S3Client.builder()
                .region(Region.of(DEFAULT_REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        CSVReader reader = null;
        List<Object[]> data = new ArrayList<>();
        
        try {
            // Read CSV file from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(DEFAULT_BUCKET)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            reader = new CSVReader(new InputStreamReader(s3Object));
            
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                }
            }
            
            System.out.println("Successfully processed " + data.size() + " rows from S3: s3://" + DEFAULT_BUCKET + "/" + s3Key);
            
        } catch (IOException e) {
            System.err.println("Error reading CSV from S3: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            s3Client.close();
        }
    }

    public static void main(String[] args) {
        // Example usage: Read CSV from S3 instead of local file system
        // The S3 key should be provided via environment variable or command line argument
        String s3Key = System.getenv().getOrDefault("CSV_S3_KEY", "csv-files/sample.csv");
        
        if (args.length > 0) {
            s3Key = args[0];
        }
        
        System.out.println("Reading CSV from S3: s3://" + DEFAULT_BUCKET + "/" + s3Key);
        processCSVFromS3(s3Key);
    }

}
