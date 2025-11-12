package school.sptech;

import school.sptech.LogsExtracao.Log;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TratamentoDados {

    private static final List<DateTimeFormatter> formatador_data_hora = List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    public static Dados tratarLinha(List<String> dados_linha, Integer numero_linha) {
        try {
            if (dados_linha == null || dados_linha.size() <= IndicesColunas.CODIGOANAC.getIndice()) {
                Log.erro("Linha " + numero_linha + " inválida (incompleta ou nula). Pulando.");
                return null;
            }

            String uf_teste = dados_linha.get(IndicesColunas.UF.getIndice());
            if (uf_teste != null && uf_teste.trim().equalsIgnoreCase("uf")) {
                Log.info("Linha " + numero_linha + " identificada como cabeçalho duplicado. Pulando.");
                return null;
            }

            String nome_fantasia_str = dados_linha.get(IndicesColunas.NOMEFANTASIA.getIndice());
            if (nome_fantasia_str == null || nome_fantasia_str.trim().isEmpty()) {
                return null;
            }

            LocalDate data_abertura = converterStringParaData(dados_linha.get(IndicesColunas.DATAABERTURA.getIndice()), numero_linha, "Data Abertura");
            LocalDateTime data_hora_resposta = converterStringParaDataHora(dados_linha.get(IndicesColunas.DATAAMERESPOSTA.getIndice()), numero_linha, "Data Hora Resposta");
            LocalDate data_finalizacao = converterStringParaData(dados_linha.get(IndicesColunas.DATAFINALIZACAO.getIndice()), numero_linha, "Data Finalização");
            Integer tempo_resposta = converterStringParaInteger(dados_linha.get(IndicesColunas.TEMPORESPOSTA.getIndice()), numero_linha, "Tempo Resposta");
            Integer nota_consumidor = converterStringParaInteger(dados_linha.get(IndicesColunas.NOTACONSUMIDOR.getIndice()), numero_linha, "Nota do Consumidor");

            String nome_fantasia = padronizarString(nome_fantasia_str);
            String uf = padronizarString(uf_teste);
            String cidade = padronizarString(dados_linha.get(IndicesColunas.CIDADE.getIndice()));
            String assunto = padronizarString(dados_linha.get(IndicesColunas.ASSUNTO.getIndice()));
            String grupo_problema = padronizarString(dados_linha.get(IndicesColunas.GRUPOPROBLEMA.getIndice()));
            String problema = padronizarString(dados_linha.get(IndicesColunas.PROBLEMA.getIndice()));
            String forma_contrato = padronizarString(dados_linha.get(IndicesColunas.FORMACONTRATO.getIndice()));
            String respondida = padronizarString(dados_linha.get(IndicesColunas.RESPONDIDA.getIndice()));
            String situacao = padronizarString(dados_linha.get(IndicesColunas.SITUACAO.getIndice()));
            String avaliacao_reclamacao = padronizarString(dados_linha.get(IndicesColunas.AVALIACAO.getIndice()));
            String codigo_anac = padronizarString(dados_linha.get(IndicesColunas.CODIGOANAC.getIndice()));

            return new Dados(uf, cidade, data_abertura, data_hora_resposta, data_finalizacao, tempo_resposta, nome_fantasia, assunto, grupo_problema, problema, forma_contrato, respondida, situacao, avaliacao_reclamacao, nota_consumidor, codigo_anac);

        } catch (IndexOutOfBoundsException iobe) {
            Log.erro("Erro de índice fora dos limites na linha " + numero_linha + ". Detalhes: " + iobe.getMessage());
            return null;
        } catch (Exception e) {
            Log.erro("ERRO CRÍTICO no tratamento da linha " + numero_linha + ": " + e.getMessage());
            return null;
        }
    }

    public static String padronizarString(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.equalsIgnoreCase("null")) {
            return null;
        }
        String texto_normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        String texto_sem_acentos = texto_normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return texto_sem_acentos.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    public static Integer converterStringParaInteger(String texto, Integer numero_linha, String nome_campo) {
        if (texto == null || texto.trim().isEmpty() || texto.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            String texto_normalizado = texto.trim().replace(',', '.');
            Double valor_double = Double.parseDouble(texto_normalizado);
            return valor_double.intValue();
        } catch (NumberFormatException e) {
            Log.erro(String.format("Não foi possível converter '%s' para número na linha %d, campo '%s'. Retornando null.", texto, numero_linha, nome_campo));
            return null;
        }
    }

    public static LocalDateTime tentarParse(String texto, DateTimeFormatter formatador) {
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

    public static LocalDateTime converterStringParaDataHora(String texto, Integer numero_linha, String nome_campo) {
        if (texto == null || texto.trim().isEmpty() || texto.equalsIgnoreCase("null")) {
            return null;
        }
        String texto_limpo = texto.trim();
        for (int i = 0; i < formatador_data_hora.size(); i++) {
            DateTimeFormatter formatador = formatador_data_hora.get(i);
            LocalDateTime resultado = tentarParse(texto_limpo, formatador);
            if (resultado != null) {
                return resultado;
            }
        }
        Log.erro(String.format("Não foi possível converter '%s' para DataHora na linha %d, campo '%s'. Formatos tentados: %s. Retornando null.", texto, numero_linha, nome_campo, formatador_data_hora.toString()));
        return null;
    }

    public static LocalDate converterStringParaData(String texto, Integer numero_linha, String nome_campo) {
        LocalDateTime data_hora = converterStringParaDataHora(texto, numero_linha, nome_campo);
        if (data_hora != null) {
            return data_hora.toLocalDate();
        }
        return null;
    }
}