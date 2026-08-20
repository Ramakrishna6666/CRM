package crm.service;

import com.azure.core.util.BlobContainerAsyncClient;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Azure Blob Storage Service for cloud-native file operations.
 * Replaces local file system writes with Azure Blob Storage for data durability
 * and availability across container restarts and scaling events.
 */
@Service
@Slf4j
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;

    @Value("${azure.storage.account-name:#{null}}")
    private String accountName;

    @Value("${azure.storage.container-name:pdf-files}")
    private String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        try {
            // Initialize Azure Blob Storage client
            // Priority 1: Use connection string if provided (for development/testing)
            // Priority 2: Use Managed Identity with account name (recommended for production)
            if (connectionString != null && !connectionString.isEmpty()) {
                log.info("Initializing Azure Blob Storage with connection string");
                blobServiceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();
            } else if (accountName != null && !accountName.isEmpty()) {
                log.info("Initializing Azure Blob Storage with Managed Identity");
                String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
                blobServiceClient = new BlobServiceClientBuilder()
                        .endpoint(endpoint)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();
            } else {
                log.warn("Azure Blob Storage not configured. Set AZURE_STORAGE_CONNECTION_STRING or AZURE_STORAGE_ACCOUNT_NAME environment variable.");
                return;
            }

            // Get or create container
            containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
                log.info("Created Azure Blob Storage container: {}", containerName);
            } else {
                log.info("Using existing Azure Blob Storage container: {}", containerName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Azure Blob Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Azure Blob Storage initialization failed", e);
        }
    }

    /**
     * Upload a file to Azure Blob Storage
     *
     * @param fileName The name of the file (blob name)
     * @param data     The file content as byte array
     * @return The URL of the uploaded blob
     */
    public String uploadFile(String fileName, byte[] data) {
        if (containerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not initialized. Check configuration.");
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            
            // Upload the file
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                blobClient.upload(inputStream, data.length, true);
            }
            
            String blobUrl = blobClient.getBlobUrl();
            log.info("Successfully uploaded file to Azure Blob Storage: {}", blobUrl);
            return blobUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to Azure Blob Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /**
     * Upload a file to Azure Blob Storage from an InputStream
     *
     * @param fileName    The name of the file (blob name)
     * @param inputStream The input stream containing file data
     * @param length      The length of the data
     * @return The URL of the uploaded blob
     */
    public String uploadFile(String fileName, InputStream inputStream, long length) {
        if (containerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not initialized. Check configuration.");
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(inputStream, length, true);
            
            String blobUrl = blobClient.getBlobUrl();
            log.info("Successfully uploaded file to Azure Blob Storage: {}", blobUrl);
            return blobUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to Azure Blob Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /**
     * Download a file from Azure Blob Storage
     *
     * @param fileName The name of the file (blob name)
     * @return The file content as byte array
     */
    public byte[] downloadFile(String fileName) {
        if (containerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not initialized. Check configuration.");
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);
            
            log.info("Successfully downloaded file from Azure Blob Storage: {}", fileName);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to download file from Azure Blob Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from Azure Blob Storage", e);
        }
    }

    /**
     * Delete a file from Azure Blob Storage
     *
     * @param fileName The name of the file (blob name)
     */
    public void deleteFile(String fileName) {
        if (containerClient == null) {
            throw new IllegalStateException("Azure Blob Storage is not initialized. Check configuration.");
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.delete();
            log.info("Successfully deleted file from Azure Blob Storage: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from Azure Blob Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from Azure Blob Storage", e);
        }
    }

    /**
     * Check if a file exists in Azure Blob Storage
     *
     * @param fileName The name of the file (blob name)
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String fileName) {
        if (containerClient == null) {
            return false;
        }

        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            return blobClient.exists();
        } catch (Exception e) {
            log.error("Failed to check file existence in Azure Blob Storage: {}", e.getMessage(), e);
            return false;
        }
    }
}
