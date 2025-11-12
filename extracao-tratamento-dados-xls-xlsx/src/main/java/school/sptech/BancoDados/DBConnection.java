package school.sptech.BancoDados;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import school.sptech.Dados;
import school.sptech.LogsExtracao.Log;
import school.sptech.TratamentoDados; // <<< 1. IMPORTAR A CLASSE

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBConnection {

    private final DataSource data_source;
    private final JdbcTemplate jdbc_template;
    private final Map<String, Integer> ids_empresas;

    public DBConnection() {
        String host = System.getenv("DB_HOST");
        String bd = System.getenv("MYSQL_DATABASE");
        String user_bd = System.getenv("MYSQL_USER");
        String senha_bd = System.getenv("MYSQL_PASSWORD");

        if (host == null || bd == null || user_bd == null || senha_bd == null) {
            Log.erro("ERRO CRÍTICO: Variáveis de ambiente do banco de dados (DB_HOST, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD) não estão definidas!");
            throw new IllegalStateException("Variáveis de ambiente do banco de dados não configuradas.");
        }

        BasicDataSource basic_data_source = new BasicDataSource();

        String url = String.format("jdbc:mysql://%s:3306/%s?rewriteBatchedStatements=true", host, bd);

        Log.info("Conectando ao banco de dados: " + url);
        basic_data_source.setUrl(url);
        basic_data_source.setUsername(user_bd);
        basic_data_source.setPassword(senha_bd);
        basic_data_source.setDriverClassName("com.mysql.cj.jdbc.Driver");

        basic_data_source.setMaxTotal(20);
        basic_data_source.setMaxWaitMillis(10000);
        basic_data_source.setDefaultQueryTimeout(300);

        this.data_source = basic_data_source;
        this.jdbc_template = new JdbcTemplate(this.data_source);

        Log.info("Iniciando ids de empresas...");
        this.ids_empresas = new HashMap<>();
        try {
            String sql_busca_todas_empresas = "SELECT idEmpresa, nomeFantasia FROM empresa";
            jdbc_template.query(sql_busca_todas_empresas, (rs) -> {
                String nome = rs.getString("nomeFantasia");
                Integer id = rs.getInt("idEmpresa");
                if (nome != null) {

                    String nome_padronizado = TratamentoDados.padronizarString(nome);

                    ids_empresas.put(nome_padronizado, id);
                }
            });
            Log.sucesso("ids de %d empresas preenchido.".formatted(ids_empresas.size()));
        } catch (Exception e) {
            Log.erro("ERRO CRÍTICO AO CRIAR IDS DE EMPRESAS: " + e.getMessage());
            throw new RuntimeException("Falha ao criar cache de empresas", e);
        }
    }

    public JdbcTemplate getConnection() {
        return this.jdbc_template;
    }

    public void insercaoDadosEmLote(List<Dados> lote_dados) {
        if (lote_dados == null || lote_dados.isEmpty()) {
            return;
        }

        String sql_insert = """
                INSERT INTO reclamacoes (
                    uf, cidade, data_abertura, data_hora_resposta, data_finalizacao,
                    tempo_resposta, nome_fantasia, assunto, grupo_problema, problema,
                    forma_contrato, respondida, situacao, avaliacao, nota_consumidor, codigo_anac, fkEmpresa
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            jdbc_template.execute((Connection con) -> {
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement(sql_insert)) {
                    Integer registros_no_lote = 0;

                    for (int i = 0; i < lote_dados.size(); i++) {
                        Dados dados = lote_dados.get(i);

                        Integer id_empresa = this.ids_empresas.get(dados.getNomeFantasia());

                        if (id_empresa != null) {
                            ps.setString(1, dados.getUf());
                            ps.setString(2, dados.getCidade());
                            ps.setObject(3, dados.getDataAbertura());
                            ps.setObject(4, dados.getDataHoraResposta());
                            ps.setObject(5, dados.getDataFinalizacao());
                            ps.setObject(6, dados.getTempoResposta());
                            ps.setString(7, dados.getNomeFantasia());
                            ps.setString(8, dados.getAssunto());
                            ps.setString(9, dados.getGrupoProblema());
                            ps.setString(10, dados.getProblema());
                            ps.setString(11, dados.getFormaContrato());
                            ps.setString(12, dados.getRespondida());
                            ps.setString(13, dados.getSituacao());
                            ps.setString(14, dados.getAvaliacao());
                            ps.setObject(15, dados.getNotaConsumidor());
                            ps.setString(16, dados.getCodigoANAC());
                            ps.setInt(17, id_empresa);

                            ps.addBatch();
                            registros_no_lote++;
                        } else {
                            Log.erro("AVISO (Lote): Empresa não encontrada no IDS EMPRESA: '" + dados.getNomeFantasia() + "'. Registro não será inserido.");
                        }
                    }

                    if (registros_no_lote > 0) {
                        Log.info("Executando lote de " + registros_no_lote + " registros no banco...");
                        ps.executeBatch();
                        con.commit();
                        Log.sucesso("Lote inserido com sucesso!");
                    } else {
                        Log.info("Nenhum registro válido para inserir neste lote.");
                    }

                } catch (SQLException e) {
                    Log.erro("Erro ao executar lote de inserção. Iniciando rollback...");
                    con.rollback();
                    Log.erro("Rollback concluído. Erro: " + e.getMessage());
                    throw new DataAccessException("Erro no batch", e) {
                    };
                } finally {
                    con.setAutoCommit(true);
                }
                return null;
            });

        } catch (Exception e) {
            Log.erro("Erro CRÍTICO na execução do lote: " + e.getMessage());
            insertLog("Erro CRÍTICO na execução do lote: " + e.getMessage(), "erro");
            throw new RuntimeException("Falha crítica no lote: " + e.getMessage(), e);
        }
    }

    public Boolean truncateDadosReclamacoe() {
        try {
            String sql = "Truncate reclamacoes;";
            this.jdbc_template.update(sql);
            Log.info("Tabela 'reclamacoes' foi limpa (TRUNCATE).");
            return true;
        } catch (Exception e) {
            Log.erro("Erro ao limpar dados de reclamações: " + e.getMessage());
            return false;
        }
    }

    public void insertLog(String log, String status) {
        try {
            String sql = "INSERT log (logMensagem, status) VALUES (?, ?)";
            this.jdbc_template.update(sql, log, status);
        } catch (Exception e) {
            Log.erro("[ERRO] [Erro ao inserir log no banco: '" + e.getMessage() + "']");
        }
    }
}