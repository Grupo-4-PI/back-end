package school.sptech.aws;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Provider {

    public static S3Client getClient() {
        String awsRegion = System.getenv("AWS_REGION");

        if (awsRegion == null || awsRegion.isBlank()) {
            throw new IllegalStateException("A variável de ambiente 'AWS_REGION' não está definida.");
        }

        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}