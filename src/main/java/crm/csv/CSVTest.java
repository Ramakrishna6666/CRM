package crm.csv;

import com.opencsv.CSVReader;
import crm.utils.ReadDataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Test utility migrated to use Azure Blob Storage for cloud-native file operations.
 * This class demonstrates reading CSV files from Azure Blob Storage instead of local file system.
 */
public class CSVTest {

    public static void main(String[] args) {
        // Configuration: Set the blob name and container via environment variables or parameters
        // Example: AZURE_STORAGE_CONTAINER_NAME=csv-files
        // Blob name should be provided as a parameter or environment variable
        String blobName = System.getenv("CSV_BLOB_NAME");
        if (blobName == null || blobName.isEmpty()) {
            // Default blob name for testing - should be configured via environment variable
            blobName = "sample.csv";
            System.out.println("Using default blob name: " + blobName);
            System.out.println("Set CSV_BLOB_NAME environment variable to specify a different blob");
        }
        
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER_NAME");
        if (containerName == null || containerName.isEmpty()) {
            System.err.println("ERROR: AZURE_STORAGE_CONTAINER_NAME environment variable must be set");
            System.err.println("Please configure Azure Storage settings:");
            System.err.println("  - AZURE_STORAGE_CONNECTION_STRING or AZURE_STORAGE_ACCOUNT_NAME");
            System.err.println("  - AZURE_STORAGE_CONTAINER_NAME");
            return;
        }

        // Read CSV file from Azure Blob Storage using InputStream (cloud-native approach)
        try (InputStream inputStream = ReadDataUtils.readFileAsStream(blobName, containerName)) {
            if (inputStream == null) {
                System.err.println("Failed to read blob from Azure Storage: " + blobName);
                System.err.println("Please ensure:");
                System.err.println("  1. Azure Storage credentials are configured");
                System.err.println("  2. The blob exists in the container: " + containerName);
                System.err.println("  3. The blob name is correct: " + blobName);
                return;
            }
            
            // Use InputStreamReader to read from Azure Blob Storage stream
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            List<Object[]> data = new ArrayList<>();
            
            String[] line;
            int lineCount = 0;
            while ((line = reader.readNext()) != null) {
                lineCount++;
                data.add(line);
                
                // Process CSV data - example: find rows with "QUICK SUB" in column 1
                if (line.length > 1 && line[1].equals("QUICK SUB")) {
                    System.out.println(line[0] + "\t" + line[1] + "\t" + (line.length > 2 ? line[2] : ""));
                }
            }
            
            reader.close();
            System.out.println("Successfully processed " + lineCount + " lines from Azure Blob Storage");
            
        } catch (IOException e) {
            System.err.println("Error reading CSV from Azure Blob Storage: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        
        /*
         * Alternative approach: Download blob to temporary file first
         * This is useful when the CSV library requires a File object
         * 
        File document = ReadDataUtils.ReadFile(blobName, containerName, "Only CSV Files", "csv");
        if (document == null) {
            System.err.println("Failed to download CSV file from Azure Storage");
            return;
        }
        
        CSVReader reader;
        List<Object[]> data = new ArrayList<>();
        try {
            reader = new CSVReader(new FileReader(document));
            String[] line;
            while ((line = reader.readNext()) != null) {
                data.add(line);
                if(line[1].equals("QUICK SUB")){
                    System.out.println(line[0] + "\t" + line[1] + "\t" + line[2]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
