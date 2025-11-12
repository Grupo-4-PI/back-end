package school.sptech;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import school.sptech.BancoDados.DBConnection;
import school.sptech.LogsExtracao.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExtrairDadosPlanilha {

    private static final Integer TAMANHO_DO_LOTE = 5000;

    private final DBConnection db_connection;
    private final DataFormatter data_formatter;

    public ExtrairDadosPlanilha(DBConnection db_connection) {
        this.db_connection = db_connection;
        this.data_formatter = new DataFormatter();
    }

    public void extrairTratarDados(InputStream arquivo_stream, String nome_arquivo) throws Exception {
        Log.info("Abrindo workbook (planilha) para o arquivo: " + nome_arquivo);

        try (Workbook workbook = new XSSFWorkbook(arquivo_stream)) {
            Sheet planinha = workbook.getSheetAt(0);
            Iterator<Row> iterador_linhas = planinha.rowIterator();

            if (iterador_linhas.hasNext()) {
                iterador_linhas.next();
                Log.info("Cabeçalho da planilha pulado.");
            }

            processarLinhasEmLote(iterador_linhas);
        }
    }

    private void processarLinhasEmLote(Iterator<Row> iterador_linhas) {
        List<Dados> lote_para_envio = new ArrayList<>();
        Integer linhas_lidas = 1;

        Log.info("Iniciando processamento das linhas em lotes de " + TAMANHO_DO_LOTE);

        while (iterador_linhas.hasNext()) {
            Row linha = iterador_linhas.next();
            linhas_lidas++;

            List<String> dados_linha = converterRowParaListString(linha);

            Dados dados_tratados = TratamentoDados.tratarLinha(dados_linha, linhas_lidas);

            if (dados_tratados != null) {
                lote_para_envio.add(dados_tratados);
            }

            if (lote_para_envio.size() >= TAMANHO_DO_LOTE) {
                Log.info("Enviando lote de " + lote_para_envio.size() + " registros... (Total lido: " + linhas_lidas + ")");
                db_connection.insercaoDadosEmLote(lote_para_envio);
                lote_para_envio.clear();
            }
        }

        if (!lote_para_envio.isEmpty()) {
            Log.info("Enviando lote final de " + lote_para_envio.size() + " registros...");
            db_connection.insercaoDadosEmLote(lote_para_envio);
            lote_para_envio.clear();
        }

        Log.info("Processamento de linhas concluído. Total de linhas lidas: " + linhas_lidas);
    }

    private List<String> converterRowParaListString(Row row) {
        List<String> dados_linha = new ArrayList<>();
        if (row == null) {
            return dados_linha;
        }

        Integer max_col = IndicesColunas.CODIGOANAC.getIndice() + 1;

        for (int i = 0; i < max_col.intValue(); i++) {
            Cell cell = row.getCell(i);
            String valor_celula = data_formatter.formatCellValue(cell);
            dados_linha.add(valor_celula);
        }
        return dados_linha;
    }
}