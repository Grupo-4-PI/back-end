package school.sptech;

import org.apache.poi.util.IOUtils;

import school.sptech.aws.S3Service;
import school.sptech.LogsExtracao.Log;

import java.io.InputStream;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        Integer NOVO_LIMITE_MB = 150 * 1024 * 1024; 
        IOUtils.setByteArrayMaxOverride(NOVO_LIMITE_MB);

        String nome_bucket_s3 = System.getenv("S3_BUCKET_NAME");

        if (nome_bucket_s3 == null || nome_bucket_s3.isBlank()) {
            DBConnection conexao_banco = new DBConnection();

            System.err.println("ERRO CRÍTICO: Variável de ambiente 'S3_BUCKET_NAME' não definida.");
            Log.erro("Variável de ambiente 'S3_BUCKET_NAME' não definida.");

            conexao_banco.insertLog("[ERRO] [Variável de ambiente 'S3_BUCKET_NAME' não definida]", "erro");

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
                DBConnection conexao_banco = new DBConnection();

                Log.erro("Nenhum arquivo .xlsx encontrado no bucket '" + nome_bucket_s3 + "' para processar.");

                conexao_banco.insertLog("Nenhum arquivo .xlsx encontrado no bucket '" + nome_bucket_s3 + "' para processar", "erro");

                return;
            }

            String chave_arquivo = chave_arquivo_recente_opt.get();
            Log.info("Arquivo mais recente encontrado: '" + chave_arquivo + "'. Iniciando download e processamento.");

            try (InputStream stream_planilha = servico_s3.getFileAsInputStream(nome_bucket_s3, chave_arquivo)) {
                Log.info("Download concluído. Iniciando extração, tratamento e inserção dos dados...");

                DBConnection conexao_banco = new DBConnection();

                if (conexao_banco.truncateDadosReclamacoe()){
                    extrator_planilha.extrairTratarDados(stream_planilha, chave_arquivo);
                    sucesso = true;

                    conexao_banco.insertLog("[SUCESSO] [Iniciando a aplicação]", "sucesso");
                }
            }
        } catch (RuntimeException re) {
            DBConnection conexao_banco = new DBConnection();

            Log.erro("Erro durante interação com S3: " + re.getMessage());
            re.printStackTrace();

            conexao_banco.insertLog("[ERRO] [Erro durante interação com S3: " + re.getMessage() + "]", "erro");
        } catch (Exception e) {
            DBConnection conexao_banco = new DBConnection();

            Log.erro("ERRO CRÍTICO inesperado na execução principal: " + e.getMessage());
            e.printStackTrace();

            conexao_banco.insertLog("[ERRO] [ERRO CRÍTICO inesperado na execução principal: " + e.getMessage() + "]", "erro");
        } finally {
            DBConnection conexao_banco = new DBConnection();

            if (sucesso) {
                Log.sucesso("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");

                conexao_banco.insertLog("[SUCESSO] [Aplicação finalizada com sucesso]","sucesso");
            } else {
                Log.erro("\n--- O PROCESSO FALHOU OU TERMINOU SEM PROCESSAR DADOS ---");

                conexao_banco.insertLog("[ERRO] [Aplicação falhou ou terminou sem processar dados]","erro");
            }

            Log.info("Aplicação finalizada.");
        }
    }
}