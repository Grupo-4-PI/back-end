// O arquivo deve estar em: src/main/java/school/sptech/aws/S3Service.java
package school.sptech.aws;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

/**
 * Classe de serviço responsável por realizar operações no S3.
 * Ela utiliza o S3Provider para obter um cliente S3 configurado.
 */
public class S3Service {

    private final S3Client s3Client;

    /**
     * Construtor que inicializa o serviço obtendo um cliente S3 do provider.
     */
    public S3Service() {
        // Usa a classe S3Provider para pegar um cliente pronto
        this.s3Client = S3Provider.getClient();
    }

    /**
     * Busca um arquivo no S3 e o retorna como um InputStream.
     *
     * @param bucketName O nome do bucket onde o arquivo está.
     * @param fileKey    O caminho/nome do arquivo dentro do bucket.
     * @return um InputStream do arquivo para leitura.
     * @throws RuntimeException se o arquivo não for encontrado ou ocorrer outro erro no S3.
     */
    public InputStream getFileAsInputStream(String bucketName, String fileKey) {
        System.out.printf("Iniciando download do arquivo '%s' do bucket '%s'...%n", fileKey, bucketName);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            // O metodo getObject retorna um ResponseInputStream, que é um tipo de InputStream
            return s3Client.getObject(getObjectRequest);

        } catch (S3Exception e) {
            System.err.println("Erro ao buscar arquivo no S3: " + e.awsErrorDetails().errorMessage());
            // Lança uma exceção para que a classe Main possa tratá-la
            throw new RuntimeException("Falha ao buscar arquivo no S3. Verifique o nome do bucket e a chave do arquivo.", e);
        }
    }
}