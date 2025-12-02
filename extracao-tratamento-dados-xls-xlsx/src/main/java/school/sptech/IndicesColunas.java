package school.sptech;

public enum IndicesColunas {

    UF(2),
    CIDADE(3),
    DATAABERTURA(6),
    DATAAMERESPOSTA(7),
    DATAFINALIZACAO(10),
    TEMPORESPOSTA(13),
    NOMEFANTASIA(14),
    ASSUNTO(16),
    GRUPOPROBLEMA(17),
    PROBLEMA(18),
    FORMACONTRATO(19),
    RESPONDIDA(21),
    SITUACAO(22),
    AVALIACAO(23),
    NOTACONSUMIDOR(24),
    CODIGOANAC(27);

    private final Integer indice;

    IndicesColunas(Integer indice) {
        this.indice = indice;
    }

    public Integer getIndice() {
        return indice;
    }
}