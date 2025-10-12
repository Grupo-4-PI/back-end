package school.sptech;

// Importe as classes necessárias
import school.sptech.aws.S3Service; // Nosso novo serviço para S3
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        // 1. CONFIGURAÇÕES S3
        // Removido o caminho do arquivo local e substituído pelas informações do bucket
        String bucketName = "s3-airwise"; // <-- Substitua pelo nome do seu bucket
        String fileKey = "airwise-base-de-dados-2024.xlsx"; // <-- O caminho/nome do arquivo no bucket

        System.out.println("Iniciando o programa...");

        // 2. INSTANCIAR SERVIÇOS
        S3Service s3Service = new S3Service();
        ExtrairDadosPlanilha extrator = new ExtrairDadosPlanilha();

        // 3. BUSCAR ARQUIVO DO S3 E PROCESSAR
        // Usamos try-with-resources para garantir que o stream do S3 seja fechado
        try (InputStream planilhaStream = s3Service.getFileAsInputStream(bucketName, fileKey)) {

            // Chamada para o metodo refatorado, passando o stream e a chave do arquivo (que serve como nome)
            Map<String, List<Dados>> dadosProntos = extrator.extrairTratarDados(planilhaStream, fileKey);

            //
            // --- O RESTANTE DO SEU CÓDIGO PERMANECE IDÊNTICO ---
            // A lógica para exibir os resultados já está perfeita.
            //
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