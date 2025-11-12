package school.sptech;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dados {

    private String uf;
    private String cidade;
    private LocalDate data_abertura;
    private LocalDateTime data_hora_resposta;
    private LocalDate data_finalizacao;
    private Integer tempo_resposta;
    private String nome_fantasia;
    private String assunto;
    private String grupo_problema;
    private String problema;
    private String forma_contrato;
    private String respondida;
    private String situacao;
    private String avaliacao;
    private Integer nota_consumidor;
    private String codigo_anac;

    public Dados(String uf, String cidade, LocalDate data_abertura, LocalDateTime data_hora_resposta, LocalDate data_finalizacao, Integer tempo_resposta, String nome_fantasia, String assunto, String grupo_problema, String problema, String forma_contrato, String respondida, String situacao, String avaliacao, Integer nota_consumidor, String codigo_anac) {
        this.uf = uf;
        this.cidade = cidade;
        this.data_abertura = data_abertura;
        this.data_hora_resposta = data_hora_resposta;
        this.data_finalizacao = data_finalizacao;
        this.tempo_resposta = tempo_resposta;
        this.nome_fantasia = nome_fantasia;
        this.assunto = assunto;
        this.grupo_problema = grupo_problema;
        this.problema = problema;
        this.forma_contrato = forma_contrato;
        this.respondida = respondida;
        this.situacao = situacao;
        this.avaliacao = avaliacao;
        this.nota_consumidor = nota_consumidor;
        this.codigo_anac = codigo_anac;
    }

    public Dados() {
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public void setDataAbertura(LocalDate data_abertura) {
        this.data_abertura = data_abertura;
    }

    public void setDataHoraResposta(LocalDateTime data_hora_resposta) {
        this.data_hora_resposta = data_hora_resposta;
    }

    public void setDataFinalizacao(LocalDate data_finalizacao) {
        this.data_finalizacao = data_finalizacao;
    }

    public void setTempoResposta(Integer tempo_resposta) {
        this.tempo_resposta = tempo_resposta;
    }

    public void setNomeFantasia(String nome_fantasia) {
        this.nome_fantasia = nome_fantasia;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public void setGrupoProblema(String grupo_problema) {
        this.grupo_problema = grupo_problema;
    }

    public void setProblema(String problema) {
        this.problema = problema;
    }

    public void setFormaContrato(String forma_contrato) {
        this.forma_contrato = forma_contrato;
    }

    public void setRespondida(String respondida) {
        this.respondida = respondida;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public void setNotaConsumidor(Integer nota_consumidor) {
        this.nota_consumidor = nota_consumidor;
    }

    public void setCodigoANAC(String codigo_anac) {
        this.codigo_anac = codigo_anac;
    }

    public String getUf() {
        return uf;
    }

    public String getCidade() {
        return cidade;
    }

    public LocalDate getDataAbertura() {
        return data_abertura;
    }

    public LocalDateTime getDataHoraResposta() {
        return data_hora_resposta;
    }

    public LocalDate getDataFinalizacao() {
        return data_finalizacao;
    }

    public Integer getTempoResposta() {
        return tempo_resposta;
    }

    public String getNomeFantasia() {
        return nome_fantasia;
    }

    public String getAssunto() {
        return assunto;
    }

    public String getGrupoProblema() {
        return grupo_problema;
    }

    public String getProblema() {
        return problema;
    }

    public String getFormaContrato() {
        return forma_contrato;
    }

    public String getRespondida() {
        return respondida;
    }

    public String getSituacao() {
        return situacao;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public Integer getNotaConsumidor() {
        return nota_consumidor;
    }

    public String getCodigoANAC() {
        return codigo_anac;
    }

    @Override
    public String toString() {
        return "Dados{" +
                "uf='" + uf + " || " +
                "cidade='" + cidade + " || " +
                "dataAbertura=" + data_abertura + " || " +
                "dataHoraResposta=" + data_hora_resposta + " || " +
                "dataFinalizacao=" + data_finalizacao + " || " +
                "tempoResposta=" + tempo_resposta + " || " +
                "nomeFantasia='" + nome_fantasia + " || " +
                "assunto='" + assunto + " || " +
                "grupoProblema='" + grupo_problema + " || " +
                "problema='" + problema +" || " +
                "formaContrato='" + forma_contrato + " || " +
                "respondida='" + respondida +" || " +
                "situacao='" + situacao + " || " +
                "avaliacao='" + avaliacao + " || " +
                "notaConsumidor=" + nota_consumidor + " || " +
                "codigoANAC='" + codigo_anac +
                '}';
    }
}