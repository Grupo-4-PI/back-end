// O arquivo deve estar em: src/main/java/school/sptech/aws/S3Provider.java
package school.sptech.aws;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Classe de configuração responsável por criar e fornecer um cliente S3.
 * Ela centraliza a configuração da conexão com a AWS.
 */
public class S3Provider {

    // Defina a região do seu bucket da S3. Ex: Region.US_EAST_1, Region.SA_EAST_1
    private static final Region REGION = Region.US_EAST_1;

    /**
     * Cria e retorna um cliente S3 configurado.
     * O DefaultCredentialsProvider irá procurar automaticamente as credenciais
     * nas variáveis de ambiente do seu sistema (AWS_ACCESS_KEY_ID, etc.).
     *
     * @return uma instância de S3Client pronta para uso.
     */
    public static S3Client getClient() {
        return S3Client.builder()
                .region(REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}