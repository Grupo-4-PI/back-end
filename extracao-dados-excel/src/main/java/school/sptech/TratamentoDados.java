package school.sptech;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TratamentoDados {

    private static final Integer indiceUf = 2;
    private static final Integer indiceCidade = 3;
    private static final Integer indiceDataAbertura = 6;
    private static final Integer indiceDataResposta = 7;
    private static final Integer indiceDataFinalizacao = 10;
    private static final Integer indiceTempoResposta = 13;
    private static final Integer indiceNomeFantasia = 14;
    private static final Integer indiceAssunto = 16;
    private static final Integer indiceGrupoProblema = 17;
    private static final Integer indiceProblema = 18;
    private static final Integer indiceFormaContrato = 19;
    private static final Integer indiceRespondida = 21;
    private static final Integer indiceSituacao = 22;
    private static final Integer indiceAvaliacao = 23;
    private static final Integer indiceNotaConsumidor = 24;
    private static final Integer indiceCodigoANAC = 27;

    private static final List<DateTimeFormatter> formatadorDataHora = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private String padronizarString(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.equalsIgnoreCase("null")) {
            return null;
        }

        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);

        String textoSemAcentos = textoNormalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return textoSemAcentos.trim().toLowerCase();
    }

    private Integer converterStringParaInteger(String texto, Integer numeroLinha, String nomeCampo) {
        if (texto == null || texto.trim().isEmpty() || texto.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            String textoNormalizado = texto.trim().replace(',', '.');
            Double valorDouble = Double.parseDouble(textoNormalizado);
            return valorDouble.intValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime tentarParse(String texto, DateTimeFormatter formatador) {
        try {
            return LocalDateTime.parse(texto, formatador);
        } catch (DateTimeParseException e1) {
            try {
                LocalDate data = LocalDate.parse(texto, formatador);
                return data.atStartOfDay();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private LocalDateTime converterStringParaDataHora(String texto, Integer numeroLinha, String nomeCampo) {
        if (texto == null || texto.trim().isEmpty() || texto.equalsIgnoreCase("null")) {
            return null;
        }
        for (int i = 0; i < formatadorDataHora.size(); i++) {
            LocalDateTime resultado = tentarParse(texto.trim(), formatadorDataHora.get(i));
            if (resultado != null){
                return resultado;
            }
        }
        return null;
    }

    private LocalDate converterStringParaData(String texto, Integer  numeroLinha, String nomeCampo) {
        LocalDateTime dataHora = converterStringParaDataHora(texto, numeroLinha, nomeCampo);
        if (dataHora != null){
            return dataHora.toLocalDate();
        }
        return null;
    }

    public Dados tratarLinha(List<String> dadosLinha, Integer numeroLinha) {
        try {
            String nomeFantasiaStr = dadosLinha.get(indiceNomeFantasia);

            if (nomeFantasiaStr == null || nomeFantasiaStr.trim().isEmpty()) {
                return null;
            }

            LocalDate dataAbertura = converterStringParaData(dadosLinha.get(indiceDataAbertura), numeroLinha, "Data Abertura");
            LocalDateTime dataHoraResposta = converterStringParaDataHora(dadosLinha.get(indiceDataResposta), numeroLinha, "Data Hora Resposta");
            LocalDate dataFinalizacao = converterStringParaData(dadosLinha.get(indiceDataFinalizacao), numeroLinha, "Data Finalização");
            Integer tempoResposta = converterStringParaInteger(dadosLinha.get(indiceTempoResposta), numeroLinha, "Tempo Resposta");
            Integer notaConsumidor = converterStringParaInteger(dadosLinha.get(indiceNotaConsumidor), numeroLinha, "Nota do Consumidor");

            String nomeFantasia = padronizarString(nomeFantasiaStr);
            String uf = padronizarString(dadosLinha.get(indiceUf));
            String cidade = padronizarString(dadosLinha.get(indiceCidade));
            String assunto = padronizarString(dadosLinha.get(indiceAssunto));
            String grupoProblema = padronizarString(dadosLinha.get(indiceGrupoProblema));
            String problema = padronizarString(dadosLinha.get(indiceProblema));
            String formaContrato = padronizarString(dadosLinha.get(indiceFormaContrato));
            String respondida = padronizarString(dadosLinha.get(indiceRespondida));
            String situacao = padronizarString(dadosLinha.get(indiceSituacao));
            String avaliacaoReclamacao = padronizarString(dadosLinha.get(indiceAvaliacao));
            String codigoANAC = padronizarString(dadosLinha.get(indiceCodigoANAC));

            Dados linha_dados_tratados = new Dados(
                    uf,
                    cidade,
                    dataAbertura,
                    dataHoraResposta,
                    dataFinalizacao,
                    tempoResposta,
                    nomeFantasia,
                    assunto,
                    grupoProblema,
                    problema,
                    formaContrato,
                    respondida,
                    situacao,
                    avaliacaoReclamacao,
                    notaConsumidor,
                    codigoANAC
            );

            return linha_dados_tratados;

        } catch (Exception e) {
            return null;
        }
    }
}