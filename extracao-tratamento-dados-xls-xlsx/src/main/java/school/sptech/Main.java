package school.sptech;

// 1. ADICIONE A IMPORTAÇÃO AQUI
import org.apache.poi.util.IOUtils;

import school.sptech.aws.S3Service;
import school.sptech.LogsExtracao.Log;

import java.io.InputStream;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        Integer NOVO_LIMITE_MB = 150 * 1024 * 1024; // 150MB em bytes
        IOUtils.setByteArrayMaxOverride(NOVO_LIMITE_MB);


        String nome_bucket_s3 = System.getenv("S3_BUCKET_NAME");

        if (nome_bucket_s3 == null || nome_bucket_s3.isBlank()) {
            System.err.println("ERRO CRÍTICO: Variável de ambiente 'S3_BUCKET_NAME' não definida.");
            Log.erro("Variável de ambiente 'S3_BUCKET_NAME' não definida.");
            return;
        }

        Log.info("Iniciando aplicação...");
        Log.info("Usando bucket S3: " + nome_bucket_s3);

        S3Service servico_s3 = new S3Service();
        ExtrairDadosPlanilha extrator_planilha = new ExtrairDadosPlanilha();
        Boolean sucesso = false;

        try {
            Log.info("Procurando arquivo .xlsx mais recente no bucket...");
            Optional<String> chave_arquivo_recente_opt = servico_s3.getLatestFileKey(nome_bucket_s3, ".xlsx");

            if (chave_arquivo_recente_opt.isEmpty()) {
                Log.erro("Nenhum arquivo .xlsx encontrado no bucket '" + nome_bucket_s3 + "' para processar.");
                return;
            }

            String chave_arquivo = chave_arquivo_recente_opt.get();
            Log.info("Arquivo mais recente encontrado: '" + chave_arquivo + "'. Iniciando download e processamento.");

            try (InputStream stream_planilha = servico_s3.getFileAsInputStream(nome_bucket_s3, chave_arquivo)) {

                Log.info("Download concluído. Iniciando extração, tratamento e inserção dos dados...");

                extrator_planilha.extrairTratarDados(stream_planilha, chave_arquivo);

                sucesso = true;

            }

        } catch (RuntimeException re) {
            Log.erro("Erro durante interação com S3: " + re.getMessage());
            re.printStackTrace();
        } catch (Exception e) {
            Log.erro("ERRO CRÍTICO inesperado na execução principal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sucesso) {
                Log.sucesso("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");
            } else {
                Log.erro("\n--- O PROCESSO FALHOU OU TERMINOU SEM PROCESSAR DADOS ---");
            }
            Log.info("Aplicação finalizada.");
        }
    }
}