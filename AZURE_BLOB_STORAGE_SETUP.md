# Azure Blob Storage Configuration Guide

## Overview
This application has been migrated from local file system operations to Azure Blob Storage for cloud-native file persistence. This ensures data durability and availability across container restarts and scaling events.

## Configuration

### Option 1: Connection String (Development/Testing)
Set the following environment variable:
```bash
export AZURE_STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=https;AccountName=<account-name>;AccountKey=<account-key>;EndpointSuffix=core.windows.net"
```

### Option 2: Managed Identity (Production - Recommended)
Set the following environment variable:
```bash
export AZURE_STORAGE_ACCOUNT_NAME="<your-storage-account-name>"
```

**Note:** When using Managed Identity, ensure your Azure App Service or Container App has a system-assigned or user-assigned managed identity with the following permissions:
- Storage Blob Data Contributor (for read/write operations)

### Container Name (Optional)
By default, the application uses `pdf-files` as the container name. You can override this:
```bash
export AZURE_STORAGE_CONTAINER_NAME="custom-container-name"
```

## Azure Setup Steps

### 1. Create Azure Storage Account
```bash
# Create resource group (if not exists)
az group create --name myResourceGroup --location eastus

# Create storage account
az storage account create \
  --name mystorageaccount \
  --resource-group myResourceGroup \
  --location eastus \
  --sku Standard_LRS \
  --kind StorageV2
```

### 2. Get Connection String (for Option 1)
```bash
az storage account show-connection-string \
  --name mystorageaccount \
  --resource-group myResourceGroup \
  --query connectionString \
  --output tsv
```

### 3. Configure Managed Identity (for Option 2)

#### Enable Managed Identity on App Service
```bash
az webapp identity assign \
  --name myAppService \
  --resource-group myResourceGroup
```

#### Assign Storage Blob Data Contributor Role
```bash
# Get the principal ID of the managed identity
PRINCIPAL_ID=$(az webapp identity show \
  --name myAppService \
  --resource-group myResourceGroup \
  --query principalId \
  --output tsv)

# Get the storage account ID
STORAGE_ID=$(az storage account show \
  --name mystorageaccount \
  --resource-group myResourceGroup \
  --query id \
  --output tsv)

# Assign the role
az role assignment create \
  --assignee $PRINCIPAL_ID \
  --role "Storage Blob Data Contributor" \
  --scope $STORAGE_ID
```

## Application Configuration

### In Azure App Service
Set environment variables in the Azure Portal:
1. Go to your App Service
2. Navigate to Configuration > Application settings
3. Add the following settings:
   - `AZURE_STORAGE_ACCOUNT_NAME` (for Managed Identity)
   - OR `AZURE_STORAGE_CONNECTION_STRING` (for connection string)
   - `AZURE_STORAGE_CONTAINER_NAME` (optional, defaults to `pdf-files`)

### In Azure Container Apps
```bash
az containerapp create \
  --name myapp \
  --resource-group myResourceGroup \
  --environment myEnvironment \
  --image myregistry.azurecr.io/myapp:latest \
  --env-vars \
    AZURE_STORAGE_ACCOUNT_NAME=mystorageaccount \
    AZURE_STORAGE_CONTAINER_NAME=pdf-files
```

## Code Changes Summary

### Files Modified
1. **PdfController.java** (Line 35)
   - Replaced `FileOutputStream` with `ByteArrayOutputStream`
   - Removed local file system write operation (`new FileOutputStream(fileName)`)
   - Added Azure Blob Storage upload using `AzureBlobStorageService`
   - PDF is now generated in memory and uploaded to Azure Blob Storage

2. **AzureBlobStorageService.java** (New)
   - Created new service for Azure Blob Storage operations
   - Supports both connection string and Managed Identity authentication
   - Provides methods for upload, download, delete, and file existence checks

3. **pom.xml**
   - Fixed malformed XML (missing closing tag for bouncycastle dependency)
   - Azure Blob Storage dependencies already present

4. **application.properties**
   - Added Azure Blob Storage configuration properties
   - Configured environment variable placeholders

## Benefits of This Migration

1. **Data Durability**: Files are persisted in Azure Blob Storage, not ephemeral container storage
2. **High Availability**: Azure Blob Storage provides 99.9% availability SLA
3. **Scalability**: Application can scale horizontally without data loss
4. **Container Restart Resilience**: Data survives container restarts and redeployments
5. **Cloud-Native**: Follows 12-factor app principles for cloud deployment

## Testing

### Local Testing
For local development, use a connection string:
```bash
export AZURE_STORAGE_CONNECTION_STRING="<your-connection-string>"
mvn spring-boot:run
```

### Verify Upload
After generating a PDF through the application:
1. Check Azure Portal > Storage Account > Containers > pdf-files
2. Verify the PDF file is present
3. Download and verify the content

## Troubleshooting

### Error: "Azure Blob Storage is not initialized"
- Ensure either `AZURE_STORAGE_CONNECTION_STRING` or `AZURE_STORAGE_ACCOUNT_NAME` is set
- Check application logs for initialization errors

### Error: "Failed to upload file to Azure Blob Storage"
- Verify storage account credentials
- Check network connectivity to Azure
- Ensure container exists or application has permission to create it

### Error: "This request is not authorized to perform this operation"
- For Managed Identity: Verify role assignment (Storage Blob Data Contributor)
- For Connection String: Verify the connection string is correct and has proper permissions

## Security Best Practices

1. **Use Managed Identity in Production**: Avoid storing connection strings in configuration
2. **Rotate Keys Regularly**: If using connection strings, rotate storage account keys periodically
3. **Use Private Endpoints**: Configure private endpoints for storage account in production
4. **Enable Soft Delete**: Enable soft delete on blob storage for data recovery
5. **Monitor Access**: Enable Azure Monitor and diagnostic logs for storage account

## Additional Resources

- [Azure Blob Storage Documentation](https://docs.microsoft.com/azure/storage/blobs/)
- [Managed Identity Documentation](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/)
- [Azure Storage Security Guide](https://docs.microsoft.com/azure/storage/common/storage-security-guide)
