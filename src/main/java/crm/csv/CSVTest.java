package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility that reads CSV files from Amazon S3.
 * This class demonstrates cloud-native file handling using AWS S3 instead of local file system.
 */
public class CSVTest {

    public static void main(String[] args) {
        // Read S3 key from environment variable or use default
        // Format: "path/to/file.csv" within the S3 bucket
        String s3Key = System.getenv().getOrDefault("CSV_FILE_S3_KEY", "data/sample.csv");
        String s3Bucket = System.getenv("S3_BUCKET_NAME"); // Optional: override default bucket
        
        // Download CSV file from S3 to temporary local file
        // ReadDataUtils handles S3 download and returns a temporary File object
        File document = ReadDataUtils.ReadFile(s3Key, s3Bucket, "Only CSV Files", "csv");
        
        if (document == null) {
            System.err.println("Failed to download CSV file from S3. S3 Key: " + s3Key);
            System.err.println("Please ensure:");
            System.err.println("  1. S3_BUCKET_NAME environment variable is set (or default bucket exists)");
            System.err.println("  2. AWS credentials are configured (IAM role, environment variables, or ~/.aws/credentials)");
            System.err.println("  3. The S3 object exists at the specified key: " + s3Key);
            System.err.println("  4. The application has s3:GetObject permission for the bucket");
            return;
        }
        
        System.out.println("Successfully downloaded CSV file from S3: " + s3Key);
        System.out.println("Temporary file location: " + document.getAbsolutePath());

        CSVReader reader = null;
        List<Object[]> data = new ArrayList<>();
        try {
            reader = new CSVReader(new FileReader(document));
            String[] line;
            while ((line = reader.readNext()) != null) {
//                System.out.println(line[1] + "\t" + line[2]);
                data.add(line);
                if(line.length > 1 && line[1].equals("QUICK SUB")){
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }
            }
            System.out.println("Successfully processed " + data.size() + " rows from CSV file");
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing CSV reader: " + e.getMessage());
                }
            }
            // Temporary file will be automatically deleted on JVM exit (deleteOnExit is set in ReadDataUtils)
        }
		/*System.out.println(data.get(0)[1] + "\t" + data.get(0)[2]);
		System.out.println(data.get(1)[1] + "\t" + data.get(1)[2]);*/
    }

}
