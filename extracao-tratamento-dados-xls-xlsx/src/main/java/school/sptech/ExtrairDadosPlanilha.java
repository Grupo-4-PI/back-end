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

import school.sptech.LogsExtracao.Log;

public class ExtrairDadosPlanilha {

    private static final Integer QUANTIDADE_COLUNAS_ESPERADA = 30;

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
            if (nome_arquivo.toLowerCase().endsWith(".xlsx")) {
                processarPlanilhaXLSXComStreaming(stream_entrada_arquivo, tratador_de_linha, conexao_banco_dados);
            } else if (nome_arquivo.toLowerCase().endsWith(".xls")) {
                Log.erro("Processando arquivo .xls. Arquivos grandes podem consumir memória.");
                processarPlanilhaXLSNaMemoria(stream_entrada_arquivo, tratador_de_linha, conexao_banco_dados);
            } else {
                throw new IllegalArgumentException("Formato de arquivo não suportado: " + nome_arquivo);
            }
            Log.sucesso("Processo de extração e tratamento concluído para: " + nome_arquivo);
        } catch (Exception e) {
            Log.erro("ERRO CRÍTICO durante o processo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Log.info("Finalizando processo para: " + nome_arquivo);
        }
    }

    private void processarPlanilhaXLSXComStreaming(InputStream stream_entrada_arquivo, TratamentoDados tratador_de_linha, DBConnection conexao_banco_dados) throws Exception {
        try (OPCPackage pacote_arquivo = OPCPackage.open(stream_entrada_arquivo)) {
            ReadOnlySharedStringsTable tabela_strings_compartilhadas = new ReadOnlySharedStringsTable(pacote_arquivo);
            XSSFReader leitor_xssf = new XSSFReader(pacote_arquivo);
            StylesTable tabela_estilos = leitor_xssf.getStylesTable();
            XSSFReader.SheetIterator iterador_de_abas = (XSSFReader.SheetIterator) leitor_xssf.getSheetsData();
            DataFormatter formatador_geral_celulas = new DataFormatter();

            Integer indice_da_aba = 0;

            while (iterador_de_abas.hasNext()) {

                try (InputStream stream_da_aba = iterador_de_abas.next()) {

                    String nome_da_aba = iterador_de_abas.getSheetName();
                    Log.info("Iniciando processamento STREAMING da aba [" + indice_da_aba + "]: '" + nome_da_aba + "'");

                    ManipuladorEventosAbaSAX manipulador_eventos_aba = new ManipuladorEventosAbaSAX(tratador_de_linha, conexao_banco_dados, nome_da_aba);
                    XMLReader parser_xml = SAXHelper.newXMLReader();
                    ContentHandler manipulador_poi_para_aba = new XSSFSheetXMLHandler(tabela_estilos, tabela_strings_compartilhadas, manipulador_eventos_aba, formatador_geral_celulas, false);
                    parser_xml.setContentHandler(manipulador_poi_para_aba);

                    InputSource fonte_xml_da_aba = new InputSource(stream_da_aba);
                    parser_xml.parse(fonte_xml_da_aba);

                    Log.sucesso("Processamento STREAMING da aba '" + nome_da_aba + "' concluído. Linhas processadas: " + manipulador_eventos_aba.getContadorLinhasProcessadas());
                }

                indice_da_aba++;
            }
        }
    }

    private static class ManipuladorEventosAbaSAX implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final TratamentoDados tratador_de_linha;
        private final DBConnection conexao_banco_dados;
        private final String nome_da_aba;
        private List<String> valores_celulas_linha_atual;
        private Integer numero_linha_atual_planilha = 0;
        private Integer indice_proxima_coluna_esperada = 0;
        private Boolean cabecalho_ja_foi_encontrado = false;
        private Integer indice_linha_cabecalho = -1;
        private Long contador_linhas_processadas = 0L;
        private final Integer TAMANHO_FIXO_ESPERADO_LINHA = 30;

        ManipuladorEventosAbaSAX(TratamentoDados tratador, DBConnection conexao_bd, String nome_aba) {
            this.tratador_de_linha = tratador;
            this.conexao_banco_dados = conexao_bd;
            this.nome_da_aba = nome_aba;
        }

        @Override
        public void startRow(int numero_linha) {
            this.valores_celulas_linha_atual = new ArrayList<>();
            this.numero_linha_atual_planilha = numero_linha;
            this.indice_proxima_coluna_esperada = 0;
        }

        @Override
        public void cell(String referencia_celula, String valor_celula_formatado, XSSFComment comentario_celula) {
            int indice_coluna_atual_planilha = org.apache.poi.ss.util.CellReference.convertColStringToIndex(
                    referencia_celula.replaceAll("[0-9]", "")
            );

            while (indice_proxima_coluna_esperada < indice_coluna_atual_planilha) {
                if (valores_celulas_linha_atual.size() < TAMANHO_FIXO_ESPERADO_LINHA) {
                    valores_celulas_linha_atual.add(null);
                }
                indice_proxima_coluna_esperada++;
            }

            // --- ALTERAÇÃO 1: IF TERNÁRIO SUBSTITUÍDO ---
            if (indice_proxima_coluna_esperada < TAMANHO_FIXO_ESPERADO_LINHA) {

                String valor_para_adicionar;
                if (valor_celula_formatado != null) {
                    valor_para_adicionar = valor_celula_formatado.trim();
                } else {
                    valor_para_adicionar = null;
                }
                valores_celulas_linha_atual.add(valor_para_adicionar);
            }
            // --- FIM DA ALTERAÇÃO 1 ---

            indice_proxima_coluna_esperada++;
        }

        @Override
        public void endRow(int numero_linha) {
            while (valores_celulas_linha_atual.size() < TAMANHO_FIXO_ESPERADO_LINHA) {
                valores_celulas_linha_atual.add(null);
            }

            if (!cabecalho_ja_foi_encontrado) {
                StringBuilder texto_linha_completa = new StringBuilder();
                for (int i = 0; i < TAMANHO_FIXO_ESPERADO_LINHA; i++) {
                    String valor = valores_celulas_linha_atual.get(i);
                    if (valor != null) texto_linha_completa.append(valor);
                }
                if (texto_linha_completa.toString().toLowerCase().contains("nome fantasia")) {
                    cabecalho_ja_foi_encontrado = true;
                    indice_linha_cabecalho = numero_linha;
                    Log.info("Cabeçalho encontrado na linha " + (numero_linha + 1) + " da aba '" + nome_da_aba + "'.");
                }
                return;
            }

            if (numero_linha > indice_linha_cabecalho) {
                Boolean linha_contem_apenas_celulas_vazias = true;
                for (int i = 0; i < TAMANHO_FIXO_ESPERADO_LINHA; i++) {
                    String valor = valores_celulas_linha_atual.get(i);
                    if (valor != null && !valor.trim().isEmpty()) {
                        linha_contem_apenas_celulas_vazias = false;
                        break;
                    }
                }
                if (linha_contem_apenas_celulas_vazias) return;

                Integer numero_linha_para_log = numero_linha + 1;
                List<String> linha_pronta_para_tratar = this.valores_celulas_linha_atual;

                try {
                    Dados dados_processados = tratador_de_linha.tratarLinhaEInserir(linha_pronta_para_tratar, numero_linha_para_log, conexao_banco_dados);
                    if (dados_processados != null) {
                        contador_linhas_processadas++;
                        if (contador_linhas_processadas % 100 == 0) {
                            Log.sucesso("Linha " + numero_linha_para_log + " da aba '" + nome_da_aba + "' processada (Total: " + contador_linhas_processadas + ").");
                        }
                    }
                } catch (Exception e) {
                    Log.erro("Erro no manipulador SAX ao processar linha " + numero_linha_para_log + " da aba '" + nome_da_aba + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public Long getContadorLinhasProcessadas() {
            return contador_linhas_processadas;
        }
    }

    private void processarPlanilhaXLSNaMemoria(InputStream stream_entrada_arquivo, TratamentoDados tratador, DBConnection conexao_banco_dados) throws IOException {
        Map<String, List<List<String>>> dados_brutos_por_aba = extrairDadosBrutosDeArquivoXLS(stream_entrada_arquivo);

        if (dados_brutos_por_aba == null || dados_brutos_por_aba.isEmpty()) {
            Log.erro("Nenhum dado bruto extraído do arquivo .xls.");
            return;
        }

        Log.info("Extração .xls concluída. Iniciando tratamento (em memória)...");
        Long total_registros_inseridos = 0L;
        Integer TAMANHO_FIXO_ESPERADO_LINHA = 30;

        // --- ALTERAÇÃO 2: FOR-EACH SUBSTITUÍDO POR FOR COMUM ---
        // 1. Converte o Set de chaves (nomes das abas) para uma Lista
        List<String> nomes_das_abas = new ArrayList<>(dados_brutos_por_aba.keySet());

        // 2. Usa um 'for' comum (baseado em índice) para iterar pela lista
        for (int i_aba = 0; i_aba < nomes_das_abas.size(); i_aba++) {
            String nome_da_aba = nomes_das_abas.get(i_aba); // Pega o nome da aba pelo índice

            // O código original do loop continua aqui
            List<List<String>> dados_brutos_da_aba = dados_brutos_por_aba.get(nome_da_aba);
            Long registros_inseridos_nesta_aba = 0L;
            Log.info("Processando aba (memória) '" + nome_da_aba + "'...");

            Integer indice_linha_cabecalho = -1;
            for (int i = 0; i < dados_brutos_da_aba.size(); i++) {
                List<String> linha_verificacao_cabecalho = dados_brutos_da_aba.get(i);
                while (linha_verificacao_cabecalho != null && linha_verificacao_cabecalho.size() < TAMANHO_FIXO_ESPERADO_LINHA) {
                    linha_verificacao_cabecalho.add(null);
                }
                if (linha_verificacao_cabecalho != null && linha_verificacao_cabecalho.toString().toLowerCase().contains("nome fantasia")) {
                    indice_linha_cabecalho = i;
                    Log.info("Cabeçalho encontrado na linha " + (i + 1));
                    break;
                }
            }

            if (indice_linha_cabecalho == -1) {
                Log.erro("AVISO: Cabeçalho não encontrado na aba '" + nome_da_aba + "'. Aba ignorada.");
                continue; // Pula para a próxima iteração do 'for' de abas
            }

            for (int i = indice_linha_cabecalho + 1; i < dados_brutos_da_aba.size(); i++) {
                Integer numero_linha_para_log = i + 1;
                List<String> linha_bruta_atual = dados_brutos_da_aba.get(i);

                while (linha_bruta_atual != null && linha_bruta_atual.size() < TAMANHO_FIXO_ESPERADO_LINHA) {
                    linha_bruta_atual.add(null);
                }

                if (linha_bruta_atual == null) continue;

                Boolean linha_contem_apenas_celulas_vazias = true;
                for(int j=0; j < TAMANHO_FIXO_ESPERADO_LINHA; j++){
                    String valor = linha_bruta_atual.get(j);
                    if (valor != null && !valor.trim().isEmpty()) {
                        linha_contem_apenas_celulas_vazias = false;
                        break;
                    }
                }
                if (linha_contem_apenas_celulas_vazias) continue;

                try {
                    Dados dados_processados = tratador.tratarLinhaEInserir(linha_bruta_atual, numero_linha_para_log, conexao_banco_dados);
                    if (dados_processados != null) {
                        registros_inseridos_nesta_aba++;
                        if (registros_inseridos_nesta_aba % 100 == 0) {
                            Log.sucesso("Linha " + numero_linha_para_log + " da aba '" + nome_da_aba + "' tratada e inserida (Total: " + registros_inseridos_nesta_aba + ").");
                        }
                    }
                } catch (Exception e) {
                    Log.erro("Erro inesperado ao processar linha " + numero_linha_para_log + " da aba '" + nome_da_aba + "': " + e.getMessage());
                }
            }
            Log.sucesso("Aba '" + nome_da_aba + "' (memória) processada. " + registros_inseridos_nesta_aba + " registros inseridos.");
            total_registros_inseridos += registros_inseridos_nesta_aba;
        }
        // --- FIM DA ALTERAÇÃO 2 ---

        Log.sucesso("Processamento .xls concluído. Total geral inserido: " + total_registros_inseridos);
    }

    private Map<String, List<List<String>>> extrairDadosBrutosDeArquivoXLS(InputStream stream_entrada_arquivo) throws IOException {
        Map<String, List<List<String>>> dados_extraidos_por_aba = new HashMap<>();
        DataFormatter formatador_geral_celulas = new DataFormatter();
        Workbook pasta_de_trabalho_xls = null;
        Integer TAMANHO_FIXO_ESPERADO_LINHA = 30;

        try {
            pasta_de_trabalho_xls = new HSSFWorkbook(stream_entrada_arquivo);
            for (int indice_da_aba = 0; indice_da_aba < pasta_de_trabalho_xls.getNumberOfSheets(); indice_da_aba++) {
                Sheet aba_atual = pasta_de_trabalho_xls.getSheetAt(indice_da_aba);
                List<List<String>> dados_desta_aba = new ArrayList<>();
                Log.info("Extraindo dados da aba (memória): '" + aba_atual.getSheetName() + "'");

                Integer indice_ultima_linha = aba_atual.getLastRowNum();
                for (int indice_da_linha = aba_atual.getFirstRowNum(); indice_da_linha <= indice_ultima_linha; indice_da_linha++) {
                    Row linha_atual_planilha = aba_atual.getRow(indice_da_linha);
                    List<String> valores_celulas_desta_linha = new ArrayList<>(TAMANHO_FIXO_ESPERADO_LINHA);
                    for(int k=0; k < TAMANHO_FIXO_ESPERADO_LINHA; k++) valores_celulas_desta_linha.add(null);


                    if (linha_atual_planilha != null) {
                        short indice_ultima_coluna_na_linha = linha_atual_planilha.getLastCellNum();
                        int limite_leitura_colunas = Math.min(indice_ultima_coluna_na_linha, TAMANHO_FIXO_ESPERADO_LINHA);

                        for (int indice_da_coluna = 0; indice_da_coluna < limite_leitura_colunas; indice_da_coluna++) {
                            Cell celula_atual = linha_atual_planilha.getCell(indice_da_coluna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            String valor_desta_celula = null;
                            if (celula_atual != null) {
                                valor_desta_celula = formatador_geral_celulas.formatCellValue(celula_atual).trim();
                            }
                            valores_celulas_desta_linha.set(indice_da_coluna, valor_desta_celula);
                        }
                    }
                    dados_desta_aba.add(valores_celulas_desta_linha);
                }
                dados_extraidos_por_aba.put(aba_atual.getSheetName(), dados_desta_aba);
            }
        } finally {
            if(pasta_de_trabalho_xls != null) {
                try { pasta_de_trabalho_xls.close(); } catch (IOException e) {
                    Log.erro("Erro ao fechar Workbook XLS: " + e.getMessage());
                }
            }
        }
        return dados_extraidos_por_aba;
    }
}