package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Configuration for cloud-native storage.
 * Uses DefaultCredentialsProvider which automatically uses IAM roles in cloud environments.
 */
@Configuration
public class AwsS3Config {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Creates an S3Client bean for dependency injection.
     * Uses DefaultCredentialsProvider which supports:
     * - Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
     * - System properties
     * - AWS credentials file (~/.aws/credentials)
     * - IAM roles for EC2/ECS/Lambda (recommended for cloud deployments)
     * 
     * @return Configured S3Client
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
