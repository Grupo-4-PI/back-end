package school.sptech;

import school.sptech.aws.S3Service;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        String bucketName = "s3-airwise";
        String fileKey = "airwise-base-de-dados-2024.xlsx";

        System.out.println("Iniciando o programa...");

        S3Service s3Service = new S3Service();
        ExtrairDadosPlanilha extrator = new ExtrairDadosPlanilha();

        try (InputStream planilhaStream = s3Service.getFileAsInputStream(bucketName, fileKey)) {

            Map<String, List<Dados>> dadosProntos = extrator.extrairTratarDados(planilhaStream, fileKey);

            if (dadosProntos != null && !dadosProntos.isEmpty()) {
                System.out.println("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");
                System.out.println("Resumo do tratamento:");

                List<String> nomesDasAbas = new ArrayList<>(dadosProntos.keySet());
                for (String nomeAba : nomesDasAbas) {
                    Integer quantidade = dadosProntos.get(nomeAba).size();
                    System.out.println("  - Aba '" + nomeAba + "' gerou " + quantidade + " registros válidos.");
                }

                System.out.println("\nOs dados agora estão tratados e prontos para a próxima etapa.");

                if (!nomesDasAbas.isEmpty()) {
                    String primeiraAba = nomesDasAbas.get(0);
                    List<Dados> linha = dadosProntos.get(primeiraAba);

                    if (linha != null && !linha.isEmpty()) {
                        System.out.println("\nExibindo registros válidos da aba '" + primeiraAba + "':");
                        for (Dados rec : linha) {
                            System.out.println("  " + rec);
                        }
                    }
                }
            } else {
                System.out.println("\n--- O PROCESSO FALHOU ---");
                System.out.println("Nenhum dado foi retornado pelo processo de extração.");
            }
        } catch (Exception e) {
            System.err.println("\n--- O PROCESSO FALHOU ---");
            System.err.println("Ocorreu um erro crítico na execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}