package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Configuration for cloud-native storage.
 * Provides S3Client bean for dependency injection throughout the application.
 */
@Configuration
public class AwsS3Config {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Creates S3Client bean using AWS SDK v2.
     * Uses DefaultCredentialsProvider which supports:
     * - Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
     * - System properties
     * - AWS credentials file (~/.aws/credentials)
     * - IAM instance profile credentials (for EC2)
     * - IAM container credentials (for ECS/Fargate)
     * 
     * @return Configured S3Client instance
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
