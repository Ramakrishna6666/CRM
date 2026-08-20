# Azure Blob Storage Configuration Guide

## Overview
This application has been migrated from local file system operations to Azure Blob Storage for cloud-native compatibility.

## Required Environment Variables

The following environment variables must be configured in your Azure environment:

### Option 1: Using Connection String (Development/Testing)
```bash
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=<account-name>;AccountKey=<account-key>;EndpointSuffix=core.windows.net
AZURE_STORAGE_CONTAINER_NAME=<container-name>
```

### Option 2: Using Managed Identity (Production - Recommended)
```bash
AZURE_STORAGE_ACCOUNT_NAME=<storage-account-name>
AZURE_STORAGE_CONTAINER_NAME=<container-name>
```

## Azure App Service Configuration

### Setting Environment Variables in Azure App Service:
1. Navigate to your App Service in Azure Portal
2. Go to **Configuration** > **Application settings**
3. Add the following settings:
   - `AZURE_STORAGE_ACCOUNT_NAME`: Your storage account name
   - `AZURE_STORAGE_CONTAINER_NAME`: Your container name (e.g., "csv-files", "uploads")

### Enable Managed Identity:
1. Go to **Identity** in your App Service
2. Enable **System assigned** managed identity
3. Grant the managed identity **Storage Blob Data Contributor** role on your storage account:
   - Navigate to your Storage Account
   - Go to **Access Control (IAM)**
   - Click **Add role assignment**
   - Select **Storage Blob Data Contributor**
   - Assign to your App Service's managed identity

## Azure Container Apps Configuration

### Setting Environment Variables:
```bash
az containerapp update \
  --name <app-name> \
  --resource-group <resource-group> \
  --set-env-vars \
    AZURE_STORAGE_ACCOUNT_NAME=<storage-account-name> \
    AZURE_STORAGE_CONTAINER_NAME=<container-name>
```

### Enable Managed Identity:
```bash
az containerapp identity assign \
  --name <app-name> \
  --resource-group <resource-group> \
  --system-assigned

# Grant Storage Blob Data Contributor role
az role assignment create \
  --assignee <managed-identity-principal-id> \
  --role "Storage Blob Data Contributor" \
  --scope /subscriptions/<subscription-id>/resourceGroups/<resource-group>/providers/Microsoft.Storage/storageAccounts/<storage-account-name>
```

## Usage Changes

### Before (Local File System):
```java
File document = ReadDataUtils.ReadFile("Select CSV file", null, "Only CSV Files", "csv");
```

### After (Azure Blob Storage):
```java
// Download blob to temporary file
File document = ReadDataUtils.ReadFile("myfile.csv", null, "Only CSV Files", "csv");

// Or use stream directly (more efficient)
InputStream stream = ReadDataUtils.readFileAsStream("myfile.csv", null);
```

## Migration Notes

1. **File Upload**: Files must be uploaded to Azure Blob Storage before they can be accessed
2. **Blob Names**: Use the blob name (path in container) instead of local file paths
3. **Container Structure**: Organize blobs in containers (e.g., "csv-files", "documents")
4. **Temporary Files**: The `ReadFile` method creates temporary files that are automatically cleaned up

## Security Best Practices

1. **Use Managed Identity** in production (no credentials in code or config)
2. **Rotate Keys** regularly if using connection strings
3. **Least Privilege**: Grant only necessary permissions (Storage Blob Data Reader for read-only)
4. **Network Security**: Configure storage account firewall rules
5. **Encryption**: Enable encryption at rest (enabled by default in Azure Storage)

## Testing Locally

For local development, you can use:
1. **Azure Storage Emulator** (Azurite)
2. **Connection String** from a development storage account

Example connection string for Azurite:
```bash
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;
AZURE_STORAGE_CONTAINER_NAME=test-container
```

## Troubleshooting

### Error: "Azure Storage credentials not configured"
- Ensure environment variables are set correctly
- Check that managed identity is enabled and has proper permissions

### Error: "Blob does not exist"
- Verify the blob name is correct
- Check that the container name is correct
- Ensure the blob has been uploaded to Azure Storage

### Error: "Container name must be provided"
- Set the `AZURE_STORAGE_CONTAINER_NAME` environment variable
- Or pass the container name as a parameter to the method

## Additional Resources

- [Azure Blob Storage Documentation](https://docs.microsoft.com/azure/storage/blobs/)
- [Azure SDK for Java](https://docs.microsoft.com/java/api/overview/azure/storage-blob-readme)
- [Managed Identity Documentation](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/)
