package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

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
        String sql = "INSERT INTO reclamacoes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbc.update(sql, dados.getUf(), dados.getCidade(), dados.getDataAbertura(), dados.getDataHoraResposta(), dados.getDataFinalizacao(), dados.getTempoResposta(), dados.getNomeFantasia(), dados.getAssunto(), dados.getGrupoProblema(), dados.getProblema(), dados.getFormaContrato(), dados.getRespondida(), dados.getSituacao(), dados.getAvaliacao(), dados.getNotaConsumidor(), dados.getCodigoANAC());
    }
}