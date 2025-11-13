package school.sptech;

import org.apache.poi.util.IOUtils;
import school.sptech.BancoDados.DBConnection;
import school.sptech.aws.S3Service;
import school.sptech.LogsExtracao.Log;

import java.io.InputStream;
import java.util.Optional;
import java.util.TimeZone;

public class Main {

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));

        Integer novo_limite_mb = 150 * 1024 * 1024;
        IOUtils.setByteArrayMaxOverride(novo_limite_mb);

        String nome_bucket_s3 = System.getenv("S3_BUCKET_NAME");

        DBConnection conexao_banco = null;
        try {
            conexao_banco = new DBConnection();
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO: Não foi possível conectar ao banco de dados.");
            Log.erro("Não foi possível conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (nome_bucket_s3 == null || nome_bucket_s3.isBlank()) {
            System.err.println("ERRO CRÍTICO: Variável de ambiente 'S3_BUCKET_NAME' não definida.");
            Log.erro("Variável de ambiente 'S3_BUCKET_NAME' não definida.");
            conexao_banco.insertLog("[ERRO] [Variável de ambiente 'S3_BUCKET_NAME' não definida]", "erro");
            return;
        }

        Log.info("Iniciando aplicação...");
        Log.info("Usando bucket S3: " + nome_bucket_s3);

        S3Service servico_s3 = new S3Service();

        ExtrairDadosPlanilha extrator_planilha = new ExtrairDadosPlanilha(conexao_banco);
        Boolean sucesso = false;

        try {
            Log.info("Procurando arquivo .xlsx mais recente no bucket...");
            Optional<String> chave_arquivo_recente_opt = servico_s3.getLatestFileKey(nome_bucket_s3, ".xlsx");

            if (chave_arquivo_recente_opt.isEmpty()) {
                String msg_erro = "Nenhum arquivo .xlsx encontrado no bucket '" + nome_bucket_s3 + "' para processar.";
                Log.erro(msg_erro);
                conexao_banco.insertLog(msg_erro, "erro");
                return;
            }

            String chave_arquivo = chave_arquivo_recente_opt.get();
            Log.info("Arquivo mais recente encontrado: '" + chave_arquivo + "'. Iniciando download e processamento.");

            try (InputStream stream_planilha = servico_s3.getFileAsInputStream(nome_bucket_s3, chave_arquivo)) {
                Log.info("Download concluído. Iniciando extração, tratamento e inserção dos dados...");

                if (conexao_banco.truncateDadosReclamacoe()) {
                    extrator_planilha.extrairTratarDados(stream_planilha, chave_arquivo);
                    sucesso = true;
                    conexao_banco.insertLog("[SUCESSO] [Iniciando a aplicação]", "sucesso");
                }
            }
        } catch (RuntimeException re) {
            String msg_erro = "Erro durante interação com S3: " + re.getMessage();
            Log.erro(msg_erro);
            re.printStackTrace();
            conexao_banco.insertLog("[ERRO] [" + msg_erro + "]", "erro");
        } catch (Exception e) {
            String msg_erro = "ERRO CRÍTICO inesperado na execução principal: " + e.getMessage();
            Log.erro(msg_erro);
            e.printStackTrace();
            conexao_banco.insertLog("[ERRO] [" + msg_erro + "]", "erro");
        } finally {
            if (sucesso) {
                Log.sucesso("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");
                conexao_banco.insertLog("[SUCESSO] [Aplicação finalizada com sucesso]", "sucesso");
            } else {
                Log.erro("\n--- O PROCESSO FALHOU OU TERMINOU SEM PROCESSAR DADOS ---");
                conexao_banco.insertLog("[ERRO] [Aplicação falhou ou terminou sem processar dados]", "erro");
            }
            Log.info("Aplicação finalizada.");
        }
    }
}