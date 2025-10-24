package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import school.sptech.LogsExtracao.Log;

import javax.sql.DataSource;

public class DBConnection {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DBConnection() {
        String host = System.getenv("DB_HOST");
        String bd = System.getenv("MYSQL_DATABASE");
        String user_bd = System.getenv("MYSQL_USER");
        String senha_bd = System.getenv("MYSQL_PASSWORD");

        if (host == null || bd == null || user_bd == null || senha_bd == null) {
            Log.erro("ERRO CRÍTICO: Variáveis de ambiente do banco de dados (DB_HOST, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD) não estão definidas!");
            throw new IllegalStateException("Variáveis de ambiente do banco de dados não configuradas.");
        }

        BasicDataSource basicDataSource = new BasicDataSource();

        String url = String.format("jdbc:mysql://%s:3306/%s", host, bd);
        Log.info("Conectando ao banco de dados: " + url);
        basicDataSource.setUrl(url);

        basicDataSource.setUsername(user_bd);
        basicDataSource.setPassword(senha_bd);

        basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        this.dataSource = basicDataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    public JdbcTemplate getConnection() {
        return this.jdbcTemplate;
    }


    public void insercaoDados(Dados dados) {
        String sqlBuscaId = "SELECT idEmpresa FROM empresa WHERE nomeFantasia = ?";
        Integer idEmpresa;

        try {
            idEmpresa = this.jdbcTemplate.queryForObject(sqlBuscaId, Integer.class, dados.getNomeFantasia());
        } catch (EmptyResultDataAccessException e) {
            Log.erro("AVISO: Empresa não encontrada no banco: '" + dados.getNomeFantasia() + "'. Registro não será inserido.");
            return;
        } catch (Exception e) {
            Log.erro("Erro ao buscar idEmpresa para '" + dados.getNomeFantasia() + "': " + e.getMessage());
            return;
        }


        String sqlInsert = """
        INSERT INTO reclamacoes (
            uf, cidade, data_abertura, data_hora_resposta, data_finalizacao, 
            tempo_resposta, nome_fantasia, assunto, grupo_problema, problema, 
            forma_contrato, respondida, situacao, avaliacao, nota_consumidor, codigo_anac, fkEmpresa
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            this.jdbcTemplate.update(sqlInsert,
                    dados.getUf(),
                    dados.getCidade(),
                    dados.getDataAbertura(),
                    dados.getDataHoraResposta(),
                    dados.getDataFinalizacao(),
                    dados.getTempoResposta(),
                    dados.getNomeFantasia(),
                    dados.getAssunto(),
                    dados.getGrupoProblema(),
                    dados.getProblema(),
                    dados.getFormaContrato(),
                    dados.getRespondida(),
                    dados.getSituacao(),
                    dados.getAvaliacao(),
                    dados.getNotaConsumidor(),
                    dados.getCodigoANAC(),
                    idEmpresa
            );
            Log.sucesso("Sucesso ao inserir reclamação para empresa '" + dados.getNomeFantasia() + "'");
        } catch (Exception e) {
            Log.erro("Erro ao inserir reclamação para empresa '" + dados.getNomeFantasia() + "': " + e.getMessage());
        }
    }
}