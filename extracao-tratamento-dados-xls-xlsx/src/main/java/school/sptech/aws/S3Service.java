// src/main/java/school/sptech/aws/S3Service.java
package school.sptech.aws;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class S3Service {

    private final S3Client s3Client;

    public S3Service() {
        this.s3Client = S3Provider.getClient();
    }

    // Seu método para baixar um arquivo continua o mesmo
    public InputStream getFileAsInputStream(String bucketName, String fileKey) {
        System.out.printf("Iniciando download do arquivo '%s' do bucket '%s'...%n", fileKey, bucketName);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (S3Exception e) {
            System.err.println("Erro ao buscar arquivo no S3: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Falha ao buscar arquivo no S3.", e);
        }
    }

    /**
     * NOVO MÉTODO: Encontra o arquivo mais recente em um bucket com base na data de modificação.
     *
     * @param bucketName O nome do bucket.
     * @param suffix     O sufixo do arquivo para filtrar (ex: ".xlsx").
     * @return um Optional contendo a chave (nome) do arquivo mais recente, ou vazio se o bucket estiver vazio.
     */
    public Optional<String> getLatestFileKey(String bucketName, String suffix) {
        System.out.println("Procurando o arquivo mais recente no bucket: " + bucketName);
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            List<S3Object> objects = s3Client.listObjectsV2(listReq).contents();

            if (objects.isEmpty()) {
                return Optional.empty(); // Retorna vazio se não houver objetos
            }

            // Usando a API de Streams do Java para encontrar o objeto com a maior data de modificação
            Optional<S3Object> latestObject = objects.stream()
                    .filter(obj -> obj.key().toLowerCase().endsWith(suffix)) // Filtra apenas arquivos com o sufixo desejado
                    .max(Comparator.comparing(S3Object::lastModified)); // Encontra o máximo pela data

            return latestObject.map(S3Object::key); // Mapeia o S3Object para sua chave (nome do arquivo)

        } catch (S3Exception e) {
            System.err.println("Erro ao listar arquivos no S3: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Falha ao listar arquivos no S3.", e);
        }
    }
}