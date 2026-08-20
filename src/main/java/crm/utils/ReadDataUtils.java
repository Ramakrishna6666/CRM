package crm.utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Cloud-native utility for reading files from Azure Blob Storage.
 * Replaces local file system dependencies with Azure Blob Storage operations.
 */
public class ReadDataUtils {

    private static final String AZURE_STORAGE_CONNECTION_STRING_ENV = "AZURE_STORAGE_CONNECTION_STRING";
    private static final String AZURE_STORAGE_ACCOUNT_NAME_ENV = "AZURE_STORAGE_ACCOUNT_NAME";
    private static final String AZURE_STORAGE_CONTAINER_NAME_ENV = "AZURE_STORAGE_CONTAINER_NAME";
    
    /**
     * Downloads a file from Azure Blob Storage to a temporary local file.
     * This method replaces the GUI-based file chooser with cloud storage access.
     * 
     * @param blobName The name of the blob to download from Azure Storage
     * @param containerName The container name (optional, uses environment variable if null)
     * @param fileExtensionDescription Description for logging purposes
     * @param fileExtension Expected file extensions for validation
     * @return File object pointing to the downloaded temporary file, or null if download fails
     */
    public static File ReadFile(String blobName, String containerName, String fileExtensionDescription,
                                String... fileExtension) {
        try {
            // Get Azure Storage configuration from environment variables
            String connectionString = System.getenv(AZURE_STORAGE_CONNECTION_STRING_ENV);
            String accountName = System.getenv(AZURE_STORAGE_ACCOUNT_NAME_ENV);
            String container = containerName != null ? containerName : System.getenv(AZURE_STORAGE_CONTAINER_NAME_ENV);
            
            if (container == null || container.isEmpty()) {
                throw new IllegalStateException("Azure Storage container name must be provided or set in environment variable: " + AZURE_STORAGE_CONTAINER_NAME_ENV);
            }
            
            // Create BlobServiceClient using connection string or managed identity
            BlobServiceClient blobServiceClient;
            if (connectionString != null && !connectionString.isEmpty()) {
                // Use connection string authentication
                blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            } else if (accountName != null && !accountName.isEmpty()) {
                // Use managed identity authentication (recommended for Azure cloud environments)
                blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();
            } else {
                throw new IllegalStateException("Azure Storage credentials not configured. Set either " + 
                    AZURE_STORAGE_CONNECTION_STRING_ENV + " or " + AZURE_STORAGE_ACCOUNT_NAME_ENV + " environment variable.");
            }
            
            // Get container client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(container);
            
            // Get blob client
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                System.err.println("Blob does not exist: " + blobName);
                return null;
            }
            
            // Validate file extension if provided
            if (fileExtension != null && fileExtension.length > 0) {
                boolean validExtension = false;
                for (String ext : fileExtension) {
                    if (blobName.toLowerCase().endsWith("." + ext.toLowerCase())) {
                        validExtension = true;
                        break;
                    }
                }
                if (!validExtension) {
                    System.err.println("File extension validation failed for: " + blobName + 
                        ". Expected: " + fileExtensionDescription);
                    return null;
                }
            }
            
            // Create temporary file to download blob content
            String fileName = blobName.substring(blobName.lastIndexOf('/') + 1);
            String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : ".tmp";
            File tempFile = Files.createTempFile("azure-blob-", extension).toFile();
            tempFile.deleteOnExit(); // Clean up on JVM exit
            
            // Download blob to temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 InputStream inputStream = blobClient.openInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            System.out.println("Successfully downloaded blob from Azure Storage: " + blobName + 
                " to temporary file: " + tempFile.getAbsolutePath());
            
            return tempFile;
            
        } catch (IOException e) {
            System.err.println("Error downloading file from Azure Blob Storage: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error accessing Azure Blob Storage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Alternative method that returns an InputStream directly from Azure Blob Storage
     * without creating a temporary file. This is more efficient for cloud environments.
     * 
     * @param blobName The name of the blob to read from Azure Storage
     * @param containerName The container name (optional, uses environment variable if null)
     * @return InputStream for reading blob content, or null if access fails
     */
    public static InputStream readFileAsStream(String blobName, String containerName) {
        try {
            // Get Azure Storage configuration from environment variables
            String connectionString = System.getenv(AZURE_STORAGE_CONNECTION_STRING_ENV);
            String accountName = System.getenv(AZURE_STORAGE_ACCOUNT_NAME_ENV);
            String container = containerName != null ? containerName : System.getenv(AZURE_STORAGE_CONTAINER_NAME_ENV);
            
            if (container == null || container.isEmpty()) {
                throw new IllegalStateException("Azure Storage container name must be provided or set in environment variable: " + AZURE_STORAGE_CONTAINER_NAME_ENV);
            }
            
            // Create BlobServiceClient
            BlobServiceClient blobServiceClient;
            if (connectionString != null && !connectionString.isEmpty()) {
                blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            } else if (accountName != null && !accountName.isEmpty()) {
                blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();
            } else {
                throw new IllegalStateException("Azure Storage credentials not configured.");
            }
            
            // Get blob client and return input stream
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(container);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                System.err.println("Blob does not exist: " + blobName);
                return null;
            }
            
            return blobClient.openInputStream();
            
        } catch (Exception e) {
            System.err.println("Error accessing Azure Blob Storage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
