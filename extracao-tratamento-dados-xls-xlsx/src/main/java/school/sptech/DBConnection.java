package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class DBConnection {

    private final DataSource dataSource;

    public DBConnection() {
        String bd = System.getenv("MYSQL_DATABASE");
        String user_bd = System.getenv("MYSQL_USER");
        String senha_bd = System.getenv("MYSQL_PASSWORD");

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/%s".formatted(bd));
        basicDataSource.setUsername("%s".formatted(user_bd));
        basicDataSource.setPassword("%s".formatted(senha_bd));

        this.dataSource = basicDataSource;
    }

    public JdbcTemplate getConnection() {
        return new JdbcTemplate(dataSource);
    }



    public void insercaoDados(Dados dados) {
        JdbcTemplate jdbc = getConnection();

        String sqlBuscaId = "SELECT idEmpresa FROM empresa WHERE nomeFantasia = ?";
        Integer idEmpresa;

        try {
            idEmpresa = jdbc.queryForObject(sqlBuscaId, Integer.class, dados.getNomeFantasia());
        } catch (EmptyResultDataAccessException e) {
            System.err.println("ERRO: Empresa não encontrada no banco de dados: " + dados.getNomeFantasia());
            return;
        }


        String sqlInsert = """
        INSERT INTO reclamacoes (
            idEmpresa, uf, cidade, dataAbertura, dataHoraResposta, dataFinalizacao, 
            tempoResposta, nomeFantasia, assunto, grupoProblema, problema, 
            formaContrato, respondida, situacao, avaliacao, notaConsumidor, codigoANAC
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbc.update(sqlInsert,
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
    }
}