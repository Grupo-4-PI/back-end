package school.sptech;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtrairDadosPlanilha {

    public Map<String, List<Dados>> extrairTratarDados(InputStream fileInputStream, String fileName) {
        try {
            System.out.println("Iniciando processo de extração para o arquivo: " + fileName);
            Map<String, List<List<String>>> dados_brutos = this.extrairDadosBrutos(fileInputStream, fileName);

            if (dados_brutos == null) return null;

            System.out.println("Extração concluída. Iniciando tratamento dos dados...");
            TratamentoDados tratador = new TratamentoDados();
            Map<String, List<Dados>> dados_aba_tratados = new HashMap<>();

            for (String nome_aba : dados_brutos.keySet()) {
                List<List<String>> dados_aba_brutos = dados_brutos.get(nome_aba);
                List<Dados> dados_tratados = new ArrayList<>();
                System.out.println("Processando aba '" + nome_aba + "'...");

                Integer indice_cabecalho = -1;
                for (int i = 0; i < dados_aba_brutos.size(); i++) {
                    List<String> linha_atual = dados_aba_brutos.get(i);
                    if (linha_atual.toString().toLowerCase().contains("nome fantasia")) {
                        indice_cabecalho = i;
                        break;
                    }
                }

                if (indice_cabecalho == -1) {
                    System.err.println("AVISO: Não foi possível encontrar a linha de cabeçalho na aba '" + nome_aba + "'. Aba ignorada.");
                    continue;
                }

                for (int i = indice_cabecalho + 1; i < dados_aba_brutos.size(); i++) {
                    Integer numero_linha_planilha = i + 1;
                    List<String> linhaBruta = dados_aba_brutos.get(i);
                    Dados dados = tratador.tratarLinha(linhaBruta, numero_linha_planilha);

                    if (dados != null) {
                        dados_tratados.add(dados);
                    }
                }
                System.out.println("Aba '" + nome_aba + "' processada. " + dados_tratados.size() + " registros válidos encontrados.");
                dados_aba_tratados.put(nome_aba, dados_tratados);
            }
            return dados_aba_tratados;

        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO no processo de extração e tratamento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, List<List<String>>> extrairDadosBrutos(InputStream fileInputStream, String fileName) throws IOException {
        IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024);

        Map<String, List<List<String>>> todos_dados_planilha = new HashMap<>();

        Workbook pasta_trabalho;
        if (fileName.toLowerCase().endsWith(".xls")) {
            pasta_trabalho = new HSSFWorkbook(fileInputStream);
        } else if (fileName.toLowerCase().endsWith(".xlsx")) {
            pasta_trabalho = new XSSFWorkbook(fileInputStream);
        } else {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + fileName);
        }

        DataFormatter data_formatter = new DataFormatter();
        try (pasta_trabalho) {
            for (Integer indice_aba = 0; indice_aba < pasta_trabalho.getNumberOfSheets(); indice_aba++) {
                Sheet aba = pasta_trabalho.getSheetAt(indice_aba);
                List<List<String>> dados_aba = new ArrayList<>();
                for (Integer indice_linha = aba.getFirstRowNum(); indice_linha <= aba.getLastRowNum(); indice_linha++) {
                    Row linha = aba.getRow(indice_linha);
                    if (linha != null) {
                        List<String> dados_linha = new ArrayList<>();
                        for (int i = 0; i < 30; i++) {
                            Cell celula = linha.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                            if (celula == null){
                                dados_linha.add(null);
                            } else {
                                dados_linha.add(data_formatter.formatCellValue(celula).trim());
                            }
                        }
                        dados_aba.add(dados_linha);
                    }
                }
                todos_dados_planilha.put(aba.getSheetName(), dados_aba);
            }
        }
        return todos_dados_planilha;
    }
}