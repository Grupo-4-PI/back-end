package school.sptech;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dados {

    private String uf;
    private String cidade;
    private LocalDate dataAbertura;
    private LocalDateTime dataHoraResposta;
    private LocalDate dataFinalizacao;
    private Integer tempoResposta;
    private String nomeFantasia;
    private String assunto;
    private String grupoProblema;
    private String problema;
    private String formaContrato;
    private String respondida;
    private String situacao;
    private String avaliacao;
    private Integer notaConsumidor;
    private String codigoANAC;

    public Dados(String uf, String cidade, LocalDate dataAbertura, LocalDateTime dataHoraResposta, LocalDate dataFinalizacao, Integer tempoResposta, String nomeFantasia, String assunto, String grupoProblema, String problema, String formaContrato, String respondida, String situacao, String avaliacao, Integer notaConsumidor, String codigoANAC) {
        this.uf = uf;
        this.cidade = cidade;
        this.dataAbertura = dataAbertura;
        this.dataHoraResposta = dataHoraResposta;
        this.dataFinalizacao = dataFinalizacao;
        this.tempoResposta = tempoResposta;
        this.nomeFantasia = nomeFantasia;
        this.assunto = assunto;
        this.grupoProblema = grupoProblema;
        this.problema = problema;
        this.formaContrato = formaContrato;
        this.respondida = respondida;
        this.situacao = situacao;
        this.avaliacao = avaliacao;
        this.notaConsumidor = notaConsumidor;
        this.codigoANAC = codigoANAC;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public void setDataHoraResposta(LocalDateTime dataHoraResposta) {
        this.dataHoraResposta = dataHoraResposta;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public void setTempoResposta(Integer tempoResposta) {
        this.tempoResposta = tempoResposta;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public void setGrupoProblema(String grupoProblema) {
        this.grupoProblema = grupoProblema;
    }

    public void setProblema(String problema) {
        this.problema = problema;
    }

    public void setFormaContrato(String formaContrato) {
        this.formaContrato = formaContrato;
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

    public void setNotaConsumidor(Integer notaConsumidor) {
        this.notaConsumidor = notaConsumidor;
    }

    public void setCodigoANAC(String codigoANAC) {
        this.codigoANAC = codigoANAC;
    }

    public String getUf() {
        return uf;
    }

    public String getCidade() {
        return cidade;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public LocalDateTime getDataHoraResposta() {
        return dataHoraResposta;
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public Integer getTempoResposta() {
        return tempoResposta;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getAssunto() {
        return assunto;
    }

    public String getGrupoProblema() {
        return grupoProblema;
    }

    public String getProblema() {
        return problema;
    }

    public String getFormaContrato() {
        return formaContrato;
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
        return notaConsumidor;
    }

    public String getCodigoANAC() {
        return codigoANAC;
    }

    @Override
    public String toString() {
        return "Dados{" +
                "uf='" + uf + " || " +
                "cidade='" + cidade + " || " +
                "dataAbertura=" + dataAbertura + " || " +
                "dataHoraResposta=" + dataHoraResposta + " || " +
                "dataFinalizacao=" + dataFinalizacao + " || " +
                "tempoResposta=" + tempoResposta + " || " +
                "nomeFantasia='" + nomeFantasia + " || " +
                "assunto='" + assunto + " || " +
                "grupoProblema='" + grupoProblema + " || " +
                "problema='" + problema +" || " +
                "formaContrato='" + formaContrato + " || " +
                "respondida='" + respondida +" || " +
                "situacao='" + situacao + " || " +
                "avaliacao='" + avaliacao + " || " +
                "notaConsumidor=" + notaConsumidor + " || " +
                "codigoANAC='" + codigoANAC +
                '}';
    }
}