package school.sptech;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Necessário para Objects.nonNull

import school.sptech.LogsExtracao.Log;

public class ExtrairDadosPlanilha {

    public void extrairTratarDados(InputStream stream_entrada_arquivo, String nome_arquivo) {
        if (nome_arquivo == null || stream_entrada_arquivo == null) {
            Log.erro("ERRO CRÍTICO: Nome do arquivo ou InputStream nulos.");
            return;
        }

        Log.info("Iniciando processo de extração e tratamento para o arquivo: " + nome_arquivo);
        TratamentoDados tratador_de_linha = new TratamentoDados();
        DBConnection conexao_banco_dados = null;

        try {
            conexao_banco_dados = new DBConnection();
            Log.info("Conexão com o banco estabelecida.");
        } catch (Exception e) {
            Log.erro("ERRO CRÍTICO ao conectar ao banco: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            Long total_linhas_processadas = 0L;

            if (nome_arquivo.toLowerCase().endsWith(".xlsx")) {
                Log.info("Processando .xlsx com STREAMING.");
                total_linhas_processadas = processarPlanilhaXLSXStreaming(stream_entrada_arquivo, tratador_de_linha, conexao_banco_dados);
            } else if (nome_arquivo.toLowerCase().endsWith(".xls")) {
                Log.erro("Processando arquivo .xls. Arquivos grandes podem consumir memória.");
                total_linhas_processadas = processarPlanilhaXLSNaMemoria(stream_entrada_arquivo, tratador_de_linha, conexao_banco_dados);
            } else {
                throw new IllegalArgumentException("Formato de arquivo não suportado: " + nome_arquivo);
            }
            Log.sucesso("Processo de extração e tratamento concluído para: " + nome_arquivo);
            Log.sucesso("Total GERAL de linhas processadas: " + total_linhas_processadas);
        } catch (Exception e) {
            Log.erro("ERRO CRÍTICO durante o processo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Log.info("Finalizando processo para: " + nome_arquivo);
        }
    }

    private Long processarPlanilhaXLSXStreaming(InputStream stream_entrada_arquivo, TratamentoDados tratador_de_linha, DBConnection conexao_banco_dados) throws Exception {
        Long contador_total_geral = 0L;
        try (OPCPackage pacote_arquivo = OPCPackage.open(stream_entrada_arquivo)) {
            ReadOnlySharedStringsTable tabela_strings_compartilhadas = new ReadOnlySharedStringsTable(pacote_arquivo);
            XSSFReader leitor_xssf = new XSSFReader(pacote_arquivo);
            StylesTable tabela_estilos = leitor_xssf.getStylesTable();
            XSSFReader.SheetIterator iterador_de_abas = (XSSFReader.SheetIterator) leitor_xssf.getSheetsData();
            DataFormatter formatador_geral_celulas = new DataFormatter();

            while (iterador_de_abas.hasNext()) {
                try (InputStream stream_da_aba = iterador_de_abas.next()) {
                    String nome_da_aba = iterador_de_abas.getSheetName();
                    Log.info("Iniciando processamento STREAMING da aba: '" + nome_da_aba + "'");

                    ProcessadorDeLinha processador_desta_aba = new ProcessadorDeLinha(tratador_de_linha, conexao_banco_dados, nome_da_aba);
                    ManipuladorEventosAbaSAX manipulador_eventos_aba = new ManipuladorEventosAbaSAX(processador_desta_aba);
                    XMLReader parser_xml = SAXHelper.newXMLReader();
                    ContentHandler manipulador_poi_para_aba = new XSSFSheetXMLHandler(tabela_estilos, tabela_strings_compartilhadas, manipulador_eventos_aba, formatador_geral_celulas, false);
                    parser_xml.setContentHandler(manipulador_poi_para_aba);

                    InputSource fonte_xml_da_aba = new InputSource(stream_da_aba);
                    parser_xml.parse(fonte_xml_da_aba);

                    Long total_processado_na_aba = processador_desta_aba.getContadorLinhasProcessadas();
                    Log.sucesso("Processamento STREAMING da aba '" + nome_da_aba + "' concluído. Linhas processadas: " + total_processado_na_aba);
                    contador_total_geral += total_processado_na_aba;
                }
            }
        }
        return contador_total_geral;
    }

    private Long processarPlanilhaXLSNaMemoria(InputStream stream_entrada_arquivo, TratamentoDados tratador_de_linha, DBConnection conexao_banco_dados) throws IOException {
        Map<String, List<List<String>>> dados_brutos_por_aba = extrairDadosBrutosDeArquivoXLS(stream_entrada_arquivo);

        if (dados_brutos_por_aba == null || dados_brutos_por_aba.isEmpty()) {
            Log.erro("Nenhum dado bruto extraído do arquivo .xls.");
            return 0L;
        }

        Log.info("Extração .xls concluída. Iniciando tratamento (em memória)...");
        Long total_registros_inseridos = 0L;

        for (Map.Entry<String, List<List<String>>> entrada_aba : dados_brutos_por_aba.entrySet()) {
            String nome_da_aba = entrada_aba.getKey();
            List<List<String>> dados_brutos_da_aba = entrada_aba.getValue();
            Log.info("Processando aba (memória) '" + nome_da_aba + "'...");

            ProcessadorDeLinha processador_desta_aba = new ProcessadorDeLinha(tratador_de_linha, conexao_banco_dados, nome_da_aba);

            for (int i = 0; i < dados_brutos_da_aba.size(); i++) {
                processador_desta_aba.processar(dados_brutos_da_aba.get(i), i);
            }

            Long registros_inseridos_nesta_aba = processador_desta_aba.getContadorLinhasProcessadas();
            Log.sucesso("Aba '" + nome_da_aba + "' (memória) processada. " + registros_inseridos_nesta_aba + " registros inseridos.");
            total_registros_inseridos += registros_inseridos_nesta_aba;
        }

        Log.sucesso("Processamento .xls concluído. Total geral inserido: " + total_registros_inseridos);
        return total_registros_inseridos;
    }

    private Map<String, List<List<String>>> extrairDadosBrutosDeArquivoXLS(InputStream stream_entrada_arquivo) throws IOException {
        Map<String, List<List<String>>> dados_extraidos_por_aba = new HashMap<>();
        DataFormatter formatador_geral_celulas = new DataFormatter();
        Workbook pasta_de_trabalho_xls = null;

        try {
            pasta_de_trabalho_xls = new HSSFWorkbook(stream_entrada_arquivo);
            for (int indice_da_aba = 0; indice_da_aba < pasta_de_trabalho_xls.getNumberOfSheets(); indice_da_aba++) {
                Sheet aba_atual = pasta_de_trabalho_xls.getSheetAt(indice_da_aba);
                List<List<String>> dados_desta_aba = new ArrayList<>();
                Log.info("Extraindo dados da aba (memória): '" + aba_atual.getSheetName() + "'");

                Integer indice_ultima_linha = aba_atual.getLastRowNum();
                for (int indice_da_linha = aba_atual.getFirstRowNum(); indice_da_linha <= indice_ultima_linha; indice_da_linha++) {
                    Row linha_atual_planilha = aba_atual.getRow(indice_da_linha);
                    List<String> valores_celulas_desta_linha = new ArrayList<>();

                    Integer limite_leitura_colunas = 0;
                    if (linha_atual_planilha != null) {
                        limite_leitura_colunas = (int) linha_atual_planilha.getLastCellNum();
                    }

                    for (int indice_da_coluna = 0; indice_da_coluna < limite_leitura_colunas; indice_da_coluna++) {
                        Cell celula_atual = null;
                        if (linha_atual_planilha != null) {
                            celula_atual = linha_atual_planilha.getCell(indice_da_coluna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        }

                        String valor_desta_celula;
                        if (celula_atual != null) {
                            valor_desta_celula = formatador_geral_celulas.formatCellValue(celula_atual).trim();
                        } else {
                            valor_desta_celula = null;
                        }
                        valores_celulas_desta_linha.add(valor_desta_celula);
                    }
                    dados_desta_aba.add(valores_celulas_desta_linha);
                }
                dados_extraidos_por_aba.put(aba_atual.getSheetName(), dados_desta_aba);
            }
        } finally {
            if (pasta_de_trabalho_xls != null) {
                try {
                    pasta_de_trabalho_xls.close();
                } catch (IOException e) {
                    Log.erro("Erro ao fechar Workbook XLS: " + e.getMessage());
                }
            }
        }
        return dados_extraidos_por_aba;
    }

    private static class ManipuladorEventosAbaSAX implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final ProcessadorDeLinha processador;
        private List<String> valores_celulas_linha_atual;
        private Integer indice_proxima_coluna_esperada = 0;

        public ManipuladorEventosAbaSAX(ProcessadorDeLinha processador) {
            this.processador = processador;
        }

        @Override
        public void startRow(int numero_linha) {
            this.valores_celulas_linha_atual = new ArrayList<>();
            this.indice_proxima_coluna_esperada = 0;
        }

        @Override
        public void cell(String referencia_celula, String valor_celula_formatado, XSSFComment comentario_celula) {
            Integer indice_coluna_atual_planilha = org.apache.poi.ss.util.CellReference.convertColStringToIndex(
                    referencia_celula.replaceAll("[0-9]", "")
            );

            while (indice_proxima_coluna_esperada < indice_coluna_atual_planilha) {
                valores_celulas_linha_atual.add(null);
                indice_proxima_coluna_esperada++;
            }

            String valor_para_adicionar;
            if (valor_celula_formatado != null) {
                valor_para_adicionar = valor_celula_formatado.trim();
            } else {
                valor_para_adicionar = null;
            }
            valores_celulas_linha_atual.add(valor_para_adicionar);
            indice_proxima_coluna_esperada++;
        }

        @Override
        public void endRow(int numero_linha) {
            processador.processar(this.valores_celulas_linha_atual, numero_linha);
        }
    }

    private static class ProcessadorDeLinha {
        private final TratamentoDados tratador_de_linha;
        private final DBConnection conexao_banco_dados;
        private final String nome_da_aba;

        private Boolean cabecalho_ja_foi_encontrado = false;
        private Integer indice_linha_cabecalho = -1;
        private Long contador_linhas_processadas = 0L;
        private Integer numero_colunas_do_cabecalho = null;

        public ProcessadorDeLinha(TratamentoDados tratador, DBConnection conexao_bd, String nome_aba) {
            this.tratador_de_linha = tratador;
            this.conexao_banco_dados = conexao_bd;
            this.nome_da_aba = nome_aba;
        }

        public void processar(List<String> linha_atual, Integer numero_linha) {
            if (linha_atual == null) {
                return;
            }

            if (!cabecalho_ja_foi_encontrado) {
                boolean contem_palavra_chave = linha_atual.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(valor -> valor.toLowerCase().contains("nome fantasia"));

                if (contem_palavra_chave) {
                    cabecalho_ja_foi_encontrado = true;
                    indice_linha_cabecalho = numero_linha;

                    Integer indice_ultimo_valor = -1;
                    for (int i = 0; i < linha_atual.size(); i++) {
                        String valor = linha_atual.get(i);
                        if (valor != null && !valor.trim().isEmpty()) {
                            indice_ultimo_valor = i;
                        }
                    }
                    this.numero_colunas_do_cabecalho = indice_ultimo_valor + 1; // +1 porque índice é base 0

                    Log.info("Cabeçalho encontrado na linha " + (numero_linha + 1) + " da aba '" + nome_da_aba + "'. Colunas detectadas: " + this.numero_colunas_do_cabecalho);
                }
                return;
            }

            if (numero_linha > indice_linha_cabecalho) {
                if (this.numero_colunas_do_cabecalho == null) {
                    Log.erro("Erro: Tentando processar linha de dados sem ter definido o número de colunas do cabeçalho na aba '" + nome_da_aba + "'. Linha ignorada: " + numero_linha);
                    return;
                }

                List<String> linha_normalizada = normalizarLinha(linha_atual);

                boolean linha_vazia = linha_normalizada.stream()
                        .allMatch(valor -> valor == null || valor.trim().isEmpty());

                if (linha_vazia) {
                    return;
                }

                Integer numero_linha_para_log = numero_linha + 1;
                try {
                    Dados dados_processados = tratador_de_linha.tratarLinhaEInserir(linha_normalizada, numero_linha_para_log, conexao_banco_dados);

                    if (dados_processados != null) {
                        contador_linhas_processadas++;
                        if (contador_linhas_processadas % 100 == 0) {
                            Log.sucesso("Linha " + numero_linha_para_log + " da aba '" + nome_da_aba + "' processada (Total: " + contador_linhas_processadas + ").");
                        }
                    }
                } catch (Exception e) {
                    Log.erro("Erro no processador de linha ao processar linha " + numero_linha_para_log + " da aba '" + nome_da_aba + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private List<String> normalizarLinha(List<String> linha) {
            if (this.numero_colunas_do_cabecalho == null) {
                Log.erro("Tentando normalizar linha sem número de colunas do cabeçalho definido na aba '" + nome_da_aba + "'. Retornando linha original.");
                return linha;
            }

            List<String> linha_normalizada = new ArrayList<>(linha);

            while (linha_normalizada.size() < this.numero_colunas_do_cabecalho) {
                linha_normalizada.add(null);
            }

            if (linha_normalizada.size() > this.numero_colunas_do_cabecalho) {
                linha_normalizada = new ArrayList<>(linha_normalizada.subList(0, this.numero_colunas_do_cabecalho));
            }
            return linha_normalizada;
        }

        public Long getContadorLinhasProcessadas() {
            return contador_linhas_processadas;
        }
    }
}