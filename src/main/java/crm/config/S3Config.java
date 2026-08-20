package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Configuration for Spring Boot.
 * Provides S3Client bean for dependency injection throughout the application.
 */
@Configuration
public class S3Config {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Creates and configures an S3Client bean.
     * Uses DefaultCredentialsProvider which checks credentials in the following order:
     * 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
     * 2. System properties
     * 3. AWS credentials file (~/.aws/credentials)
     * 4. IAM role for EC2/ECS/Lambda (recommended for production)
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
