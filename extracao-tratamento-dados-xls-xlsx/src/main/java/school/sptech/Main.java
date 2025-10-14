// src/main/java/school/sptech/Main.java
package school.sptech;

import school.sptech.aws.S3Service;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        String bucketName = System.getenv("S3_BUCKET_NAME");

        if (bucketName == null || bucketName.isBlank()) {
            System.err.println("ERRO CRÍTICO: A variável de ambiente 'S3_BUCKET_NAME' não está definida.");
            return;
        }

        System.out.println("Iniciando o programa...");
        S3Service s3Service = new S3Service();
        ExtrairDadosPlanilha extrator = new ExtrairDadosPlanilha();

        try {
            // 1. Encontra o arquivo mais recente no bucket que termine com .xlsx
            Optional<String> latestFileKeyOpt = s3Service.getLatestFileKey(bucketName, ".xlsx");

            // 2. Verifica se um arquivo foi encontrado
            if (latestFileKeyOpt.isEmpty()) {
                System.out.println("Nenhum arquivo .xlsx encontrado no bucket para processar.");
                return;
            }

            String fileKey = latestFileKeyOpt.get();
            System.out.println("Arquivo mais recente encontrado: " + fileKey + ". Iniciando processamento.");

            // 3. Baixa e processa o arquivo encontrado (sua lógica original)
            try (InputStream planilhaStream = s3Service.getFileAsInputStream(bucketName, fileKey)) {

                System.out.println("Download concluído. Iniciando extração e tratamento dos dados...");
                Map<String, List<Dados>> dadosProntos = extrator.extrairTratarDados(planilhaStream, fileKey);

                if (dadosProntos != null && !dadosProntos.isEmpty()) {
                    System.out.println("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");
                } else {
                    System.out.println("\n--- PROCESSO FINALIZADO, MAS SEM DADOS VÁLIDOS RETORNADOS ---");
                }
            }

        } catch (Exception e) {
            System.err.println("\n--- O PROCESSO FALHOU ---");
            System.err.println("Ocorreu um erro crítico na execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}