# Guia COMPLETO e linha-a-linha do código Java — Extração e Tratamento de Planilha (S3 → Dados)
**Objetivo:** este documento contém **todo o código** (4 classes + S3 stub + `pom.xml` exemplo) e **explicações detalhadas por bloco**, incluindo **cada parte importante** (variáveis, estruturas, palavras-chave) para alguém que **nunca programou**. Não omiti nada: as classes `Main`, `ExtrairDadosPlanilha`, `TratamentoDados` e `Dados` estão completas, com cada trecho acompanhado de explicação.

---
> Observação: explicações são em blocos (trecho de código seguido de explicação). Quando um elemento for especialmente importante para iniciantes (por exemplo `try`, `InputStream`, `Map`, `null`, `for`, `break`, `Workbook`, `DataFormatter`), haverá uma explicação destacada.

---

## Índice
1. Arquivos incluídos (lista)
2. `Main.java` — código completo + explicação em blocos
3. `ExtrairDadosPlanilha.java` — código completo + explicação em blocos
4. `TratamentoDados.java` — código completo + explicação em blocos
5. `Dados.java` — código completo + explicação em blocos
6. `S3Service` (stub local) — código + explicação
7. `pom.xml` (exemplo) — dependências e build
8. Formato da planilha e mapeamento de índices (explícito)
9. Execução, testes locais, debug, problemas comuns e soluções
10. Checklist final e próximas melhorias

---

## 1) Arquivos incluídos neste guia
- `Main.java`
- `ExtrairDadosPlanilha.java`
- `TratamentoDados.java`
- `Dados.java`
- `aws/S3Service.java` (stub para testes locais)
- `pom.xml` (exemplo para Maven)

---

## 2) `Main.java` — código completo

```java
package school.sptech;

import school.sptech.aws.S3Service;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        String bucketName = "s3-airwise";
        String fileKey = "airwise-base-de-dados-2024.xlsx";

        System.out.println("Iniciando o programa...");

        S3Service s3Service = new S3Service();
        ExtrairDadosPlanilha extrator = new ExtrairDadosPlanilha();

        try (InputStream planilhaStream = s3Service.getFileAsInputStream(bucketName, fileKey)) {

            Map<String, List<Dados>> dadosProntos = extrator.extrairTratarDados(planilhaStream, fileKey);

            if (dadosProntos != null && !dadosProntos.isEmpty()) {
                System.out.println("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");
                System.out.println("Resumo do tratamento:");

                List<String> nomesDasAbas = new ArrayList<>(dadosProntos.keySet());
                for (String nomeAba : nomesDasAbas) {
                    Integer quantidade = dadosProntos.get(nomeAba).size();
                    System.out.println("  - Aba '" + nomeAba + "' gerou " + quantidade + " registros válidos.");
                }

                System.out.println("\nOs dados agora estão tratados e prontos para a próxima etapa.");

                if (!nomesDasAbas.isEmpty()) {
                    String primeiraAba = nomesDasAbas.get(0);
                    List<Dados> linha = dadosProntos.get(primeiraAba);

                    if (linha != null && !linha.isEmpty()) {
                        System.out.println("\nExibindo registros válidos da aba '" + primeiraAba + "':");
                        for (Dados rec : linha) {
                            System.out.println("  " + rec);
                        }
                    }
                }
            } else {
                System.out.println("\n--- O PROCESSO FALHOU ---");
                System.out.println("Nenhum dado foi retornado pelo processo de extração.");
            }
        } catch (Exception e) {
            System.err.println("\n--- O PROCESSO FALHOU ---");
            System.err.println("Ocorreu um erro crítico na execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### Explicação em blocos — `Main.java`

**a) `package` e `import`**

```java
package school.sptech;
import school.sptech.aws.S3Service;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
```

- `package school.sptech;` — agrupa classes relacionadas. Pense como uma pasta lógica: ajuda a organizar o código. Não é obrigatório, mas é padrão em projetos Java.
- `import` — traz classes de outros pacotes: `S3Service` (sua classe para buscar arquivos no S3), `InputStream` (tipo para leitura de bytes), coleções `List`, `Map` etc.

**b) `public class Main` e método `main`**

```java
public class Main {
    public static void main(String[] args) {
```
- `public class Main` — declara a classe pública chamada `Main` (o arquivo Java deve ter o mesmo nome).
- `public static void main(String[] args)` — **ponto de entrada** do programa. Quando você executa `java Main`, esse método é chamado. `String[] args` é vetor de argumentos passados pela linha de comando (ex.: `java Main arquivo.xlsx`).

**c) Variáveis de configuração**

```java
String bucketName = "s3-airwise";
String fileKey = "airwise-base-de-dados-2024.xlsx";
```
- `String` é tipo textual. Aqui guardamos o `bucketName` (nome do balde no S3) e `fileKey` (caminho/nome do arquivo dentro do bucket). Em produção, você normalmente não "hardcoda" esses valores; preferiria recebê-los por `args` ou variáveis de ambiente.

**d) `System.out.println`**

```java
System.out.println("Iniciando o programa...");
```
- imprime no console. Útil para acompanhar execução.

**e) Instanciando serviços**

```java
S3Service s3Service = new S3Service();
ExtrairDadosPlanilha extrator = new ExtrairDadosPlanilha();
```
- `new` cria um objeto. `s3Service` e `extrator` são variáveis que guardam referências aos objetos criados.

**f) `try-with-resources`**

```java
try (InputStream planilhaStream = s3Service.getFileAsInputStream(bucketName, fileKey)) {
    // ... uso do planilhaStream ...
}
```
- `try` serve para tratar exceções. A forma `try (resource)` é chamada *try-with-resources* — garante que o recurso (`planilhaStream`) será fechado automaticamente no final do bloco, mesmo se ocorrer um erro. Isso evita vazamento de recursos (ex.: arquivos abertos).
- `InputStream` representa um fluxo de bytes; com ele você consegue ler o conteúdo do arquivo sem precisar salvá-lo em disco.

**g) Chamada principal para extração e tratamento**

```java
Map<String, List<Dados>> dadosProntos = extrator.extrairTratarDados(planilhaStream, fileKey);
```
- `Map<K,V>` é um dicionário: mapeia chaves (`String` = nome da aba) para valores (`List<Dados>` = registros tratados).
- `extrairTratarDados` é o método que faz a maior parte do trabalho.

**h) Checagem de resultado**

```java
if (dadosProntos != null && !dadosProntos.isEmpty()) {
    // sucesso
} else {
    // falha
}
```
- `!= null` verifica se o objeto existe (não é "sem valor"). Em Java, tentar acessar métodos de uma variável `null` causa erro `NullPointerException`.
- `!dadosProntos.isEmpty()` verifica se o `Map` tem entradas. `!` é negação lógica.

**i) Iterando as abas (for-each)**

```java
List<String> nomesDasAbas = new ArrayList<>(dadosProntos.keySet());
for (String nomeAba : nomesDasAbas) {
    Integer quantidade = dadosProntos.get(nomeAba).size();
    System.out.println("  - Aba '" + nomeAba + "' gerou " + quantidade + " registros válidos.");
}
```
- `dadosProntos.keySet()` retorna um `Set` (conjunto) com todos os nomes de aba.
- `for (String nomeAba : nomesDasAbas)` percorre a lista. É a forma mais simples de laço para coleções em Java.

**j) Exibição de registros**

```java
for (Dados rec : linha) {
    System.out.println("  " + rec);
}
```
- ao chamar `System.out.println` com um objeto (`rec`), o Java usa o método `toString()` desse objeto (se existir) para convertê-lo em texto.

**k) Tratamento de exceções (catch)**

```java
} catch (Exception e) {
    System.err.println("\n--- O PROCESSO FALHOU ---");
    System.err.println("Ocorreu um erro crítico na execução: " + e.getMessage());
    e.printStackTrace();
}
```
- `catch` captura exceções lançadas dentro do `try`. `Exception` é a classe-mãe de erros "esperados". Em code real é melhor capturar exceções específicas (ex.: `IOException`) para tratar de forma adequada.
- `System.err.println` envia mensagem para o canal de erro (separado do `System.out`).
- `e.printStackTrace()` imprime a pilha de chamadas que levaram ao erro — essencial para debugar.

---

## 3) `ExtrairDadosPlanilha.java` — código completo e explicação detalhada

```java
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
```

### Explicação em blocos — `ExtrairDadosPlanilha.java`

**a) Imports do Apache POI e IOUtils**

```java
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
```
- `HSSFWorkbook` → para arquivos `.xls` (Excel antigo, formato BIFF - 97-2003).  
- `XSSFWorkbook` → para arquivos `.xlsx` (Excel moderno, Office Open XML).  
- `Workbook`, `Sheet`, `Row`, `Cell` → abstrações do Apache POI para navegar na planilha.  
- `IOUtils.setByteArrayMaxOverride(...)` → ajuste interno para permitir leitura de arquivos grandes (aumenta limite de buffer).

**b) Método `extrairTratarDados`**

- Recebe `InputStream` do arquivo e `fileName` (para detectar extensão).
- Chama `extrairDadosBrutos` para ler todas as abas como `List<List<String>>` (linhas × colunas em texto).
- Instancia `TratamentoDados` para transformar cada linha em `Dados`.
- Procura a linha que contém "nome fantasia" para localizar o cabeçalho da tabela; se não encontrar, ignora a aba (mostra `AVISO`).
- Percorre linhas a partir da linha seguinte ao cabeçalho e chama `tratador.tratarLinha` para cada linha; adiciona ao `List<Dados>` se o retorno não for `null`.

**c) Método `extrairDadosBrutos`**

- `IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024);` — aumenta limite para ~500MB; sem isso, POI pode falhar em arquivos grandes.
- `Workbook pasta_trabalho;` — variável que conterá o objeto que representa toda a planilha.
- Detecta extensão do arquivo via `fileName.toLowerCase().endsWith(".xls")` ou `".xlsx"` e usa a classe apropriada (`HSSFWorkbook` ou `XSSFWorkbook`). Se não for nenhum dos dois, lança `IllegalArgumentException` com mensagem clara.
- `DataFormatter data_formatter = new DataFormatter();` — POI pode ter células com tipos (número, data, fórmula). `DataFormatter` transforma qualquer célula em String legível conforme o formato da célula (importante para manter datas como `2024-01-01` e não `44927.0` por exemplo).
- `try (pasta_trabalho) { ... }` — aqui usamos `try-with-resources` para fechar o `Workbook` no final (POI implementa `Closeable`/`AutoCloseable`).

**d) Percorrendo sheets, linhas e células**

- `for (Integer indice_aba = 0; indice_aba < pasta_trabalho.getNumberOfSheets(); indice_aba++)` — itera por cada aba pelo índice.
- Dentro de cada aba, pegamos o `Sheet` e então iteramos do `getFirstRowNum()` até `getLastRowNum()`.
- Para cada `Row linha = aba.getRow(indice_linha);` verificamos `if (linha != null)` — pois planilha pode ter linhas vazias.
- Em seguida criamos `List<String> dados_linha = new ArrayList<>();` e lemos até 30 células (`for (int i = 0; i < 30; i++)`). Esse limite de 30 é fixo no código original: **assume até 30 colunas relevantes**. Se quiser ler todas colunas, podemos trocar por `linha.getLastCellNum()` (mas cuidado com `null` em linhas curtas).
- `Row.MissingCellPolicy.RETURN_BLANK_AS_NULL` faz com que células em branco retornem `null` em vez de um objeto `Cell` vazio.
- Se `celula` é `null` (ausente), adicionamos `null` na posição correspondente; caso contrário, usamos `data_formatter.formatCellValue(celula).trim()` — converte em texto e remove espaços nas pontas.
- Ao final de cada aba, adicionamos `todos_dados_planilha.put(aba.getSheetName(), dados_aba);` — mapeamos `nome da aba` → `lista de linhas` (cada linha = `List<String>`).

**e) Por que retornar `Map<String, List<List<String>>>`?**

- É uma representação bruta da planilha (key = nome da aba; value = lista de linhas; cada linha = lista de textos). Separar leitura bruta e tratamento melhora organização e facilita testes (você pode testar `extrairDadosBrutos` isoladamente).

---

## 4) `TratamentoDados.java` — código completo e explicação detalhada

```java
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
```

### Explicação em blocos — `TratamentoDados.java`

**a) Constantes de índice (mapeamento das colunas)**

```java
private static final Integer indiceUf = 2;
// ... outros índices ...
private static final Integer indiceCodigoANAC = 27;
```
- Cada constante indica em qual **coluna** da planilha está a informação. **Importante:** esses índices começam em 0. Ou seja `indiceUf = 2` significa "terceira coluna". Ajuste se sua planilha mudar de layout.

**b) Formatadores de data**

```java
private static final List<DateTimeFormatter> formatadorDataHora = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
);
```
- Lista de formatos aceitos para tentar converter strings em datas/hora. Primeiro tenta com hora; se não, tenta só data. Para suportar `dd/MM/yyyy` adicione outro `DateTimeFormatter.ofPattern("dd/MM/yyyy")` etc.

**c) `padronizarString` — limpeza e normalização**

- Remove `null`, strings vazias e texto literal `"null"`. Uso de `Normalizer` para remover acentuação e `toLowerCase()` para uniformizar. Resultado: texto limpo, sem acento e em minúsculas.

**d) `converterStringParaInteger` — tenta converter texto para número inteiro**

- Remove espaços, troca vírgula por ponto (aceita `1,0`) e usa `Double.parseDouble`. Se falhar, retorna `null` (significa valor inválido).

**e) `tentarParse` / `converterStringParaDataHora` — conversão robusta de datas**

- `tentarParse` primeiro tenta `LocalDateTime.parse(texto, formatador)` — se falhar, tenta `LocalDate.parse(texto, formatador)` e converte para `LocalDateTime` com `atStartOfDay()`.
- `converterStringParaDataHora` chama `tentarParse` para cada `formatadorDataHora` até encontrar uma conversão válida. Retorna `null` se nenhum formato bate.

**f) `tratarLinha` — passo final que constrói o objeto `Dados`**

- Lê `nomeFantasia` primeiro; se for `null`/vazio, considera a linha inválida e retorna `null` (a linha é ignorada).
- Converte datas e números chamando os métodos auxiliares.
- Padroniza strings com `padronizarString(...)`.
- Monta um `Dados` com todos os campos tratados e retorna este objeto.
- Envolve tudo num `try/catch` que captura qualquer exceção e retorna `null` (isso protege o processamento global — linhas com erro não interrompem todo o trabalho).

---

## 5) `Dados.java` — código completo e explicação

```java
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
```

### Explicação em blocos — `Dados.java`

**a) Campos privados (`private`)**

- Campos como `private String uf;` guardam dados internos do objeto. `private` significa que somente métodos dentro da classe podem acessá-los diretamente (encapsulamento). Para acessar, usamos `get`/`set` públicos.
- Tipos: `String` (texto), `LocalDate` (data sem hora), `LocalDateTime` (data com hora), `Integer` (número inteiro).

**b) Construtor**

- Recebe todos os campos e faz `this.campo = campo;` para inicializar o objeto. É a forma padrão de montar um DTO (Data Transfer Object).

**c) Getters e Setters**

- `getX()` devolve o valor do campo `x`. `setX(...)` altera o valor do campo `x`. Padrão Java que permite controle de acesso e validação futura.

**d) `toString()`**

- É usado para exibir o objeto em formato legível quando impresso. É importante para debugging e logs.

---

## 6) `S3Service` — exemplo STUB para testes locais (caminho `school/sptech/aws/S3Service.java`)

> Atenção: este stub **não acessa a AWS**. Ele serve para facilitar testes locais lendo arquivos do disco.
> Em produção substitua por uma implementação que use AWS SDK v2 (ou v1).

```java
package school.sptech.aws;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class S3Service {
    // Stub local: lê arquivo do diretório data/ do projeto
    public InputStream getFileAsInputStream(String bucketName, String fileKey) throws Exception {
        // Aqui apenas concatena em data/<fileKey>
        Path localPath = Paths.get("data", fileKey);
        return new FileInputStream(localPath.toFile());
    }
}
```

### Explicação do `S3Service` stub

- `FileInputStream` abre um arquivo local e devolve `InputStream` (mesmo tipo esperado pelo `ExtrairDadosPlanilha`).
- Ao usar este stub, coloque o arquivo `.xlsx` em `data/airwise-base-de-dados-2024.xlsx` no root do seu projeto. Assim `Main` vai conseguir abri-lo sem AWS.

---

## 7) `pom.xml` — exemplo mínimo para Maven com Shade (gera jar executável)
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
  <modelVersion>4.0.0</modelVersion>
  <groupId>school.sptech</groupId>
  <artifactId>extrator-planilha</artifactId>
  <version>1.0.0</version>

  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi</artifactId>
      <version>5.2.3</version>
    </dependency>
    <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi-ooxml</artifactId>
      <version>5.2.3</version>
    </dependency>
    <!-- se for usar AWS SDK, adicione: -->
    <!--
    <dependency>
      <groupId>software.amazon.awssdk</groupId>
      <artifactId>s3</artifactId>
      <version>2.20.0</version>
    </dependency>
    -->
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.2.4</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <mainClass>school.sptech.Main</mainClass>
                </transformer>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

### Explicação do `pom.xml`

- Dependências `poi` e `poi-ooxml` permitem ler `.xls` e `.xlsx` respectivamente.
- `maven-shade-plugin` cria um jar "fat" que contém dependências e define a `mainClass` para execução com `java -jar`.

---

## 8) Formato da planilha e mapeamento de índices (detalhado)

O código assume que as colunas relevantes correspondem aos seguintes índices (começando de 0):

- `indiceUf = 2` → 3ª coluna
- `indiceCidade = 3` → 4ª coluna
- `indiceDataAbertura = 6` → 7ª coluna
- `indiceDataResposta = 7` → 8ª coluna
- `indiceDataFinalizacao = 10` → 11ª coluna
- `indiceTempoResposta = 13` → 14ª coluna
- `indiceNomeFantasia = 14` → 15ª coluna **(campo obrigatório — linha é ignorada se vazio)**
- `indiceAssunto = 16`
- `indiceGrupoProblema = 17`
- `indiceProblema = 18`
- `indiceFormaContrato = 19`
- `indiceRespondida = 21`
- `indiceSituacao = 22`
- `indiceAvaliacao = 23`
- `indiceNotaConsumidor = 24`
- `indiceCodigoANAC = 27`

**Importante:** ajuste os índices se a sua planilha tiver colunas deslocadas. Indices errados resultam em dados nulos ou campos trocados.

### Exemplo de linha (CSV simplificado) com índices marcados (coluna 0,1,2...)
```
col0,col1,UF(2),Cidade(3),col4,col5,DataAbertura(6),DataResposta(7),col8,col9,DataFinalizacao(10),...,TempoResposta(13),NomeFantasia(14),...,Assunto(16),GrupoProblema(17),Problema(18),FormaContrato(19),col20,Respondida(21),Situacao(22),Avaliacao(23),NotaConsumidor(24),col25,CodigoANAC(27),...
```

---

## 9) Execução, testes locais e debugging (passo a passo)

### Pré-requisitos
- JDK 17+ instalado (ou 11 se preferir; ajuste `pom.xml`).
- Maven instalado (ou use IDE como IntelliJ/Eclipse que já roda `mvn` internamente).
- Coloque sua planilha no caminho `data/airwise-base-de-dados-2024.xlsx` se usar o stub S3 do exemplo.

### Compilar e empacotar
No terminal, na raiz do projeto (onde está `pom.xml`):
```bash
mvn clean package
```
Isso gera `target/extrator-planilha-1.0.0.jar` (se shade configurado).

### Executar o jar
```bash
java -jar target/extrator-planilha-1.0.0.jar
```
Se usar IDE, apenas rode a classe `Main`.

### Testes locais (sem S3)
- Use o `S3Service` stub que lê `data/<fileKey>`. Copie o `.xlsx` para `data/`.
- Execute `Main` e acompanhe o console. Você verá mensagens de progresso (logs com `System.out.println`).

### Debug e mensagens úteis a adicionar
- Adicione `System.out.println("linha bruta: " + linhaBruta)` dentro do loop que chama `tratador.tratarLinha(...)` para ver o raw.
- Para saber por que uma linha retornou `null` no `tratador`, modifique o `catch` em `tratarLinha` para `e.printStackTrace()` e/ou adicionar um log com `numeroLinha` e `e.getMessage()`.
- Verifique se o `indiceNomeFantasia` bate com a coluna real; se estiver deslocado, todas as linhas podem estar sendo ignoradas.

### Situações comuns e soluções
- **Cabeçalho não encontrado:** a busca usa `linha_atual.toString().toLowerCase().contains("nome fantasia")`. Se o cabeçalho tiver acento/variação/sem espaço, ajuste para procurar por outras variações ou procure célula-a-célula (mais robusto).
- **Datas em formatos diferentes:** adicione novos `DateTimeFormatter` em `formatadorDataHora` (ex.: `"dd/MM/yyyy"`, `"dd/MM/yyyy HH:mm"`).
- **Números com separadores de milhar:** remova pontos antes de parse (ex.: `texto.replace(".", "").replace(',', '.')`).
- **Arquivos muito grandes:** `IOUtils.setByteArrayMaxOverride(...)` já aumenta limiar; aumente mais se necessário, ou processe por streaming (SAX) para arquivos gigantescos.
- **NullPointerException:** geralmente significa que o código tentou usar um método em um valor `null`. Verifique o stack trace e a linha correspondente no código.

---

## 10) Checklist final e sugestões de melhorias

### Checklist para rodar com sucesso
- [ ] JDK + Maven instalados
- [ ] `pom.xml` com dependências do Apache POI
- [ ] Código nas pastas correspondentes (`src/main/java/...`)
- [ ] Arquivo de teste em `data/airwise-base-de-dados-2024.xlsx` (se usar stub)
- [ ] `mvn clean package` executado com sucesso
- [ ] `java -jar target/...jar` ou execução via IDE
- [ ] Verificar logs e ajustar índices caso campos fiquem nulos

### Melhorias recomendadas (próximos passos)
1. Tornar índices configuráveis via arquivo `.properties` ou `.yaml` — sem recompilar.  
2. Implementar `S3Service` real com AWS SDK (autenticação via IAM/credenciais).  
3. Tornar detecção de cabeçalho mais robusta (procura por célula em vez de `toString()` da linha).  
4. Gerar relatório CSV com linhas inválidas e o motivo (útil para auditoria).  
5. Adicionar testes unitários (JUnit) para `tratarLinha` cobrindo datas, números, strings vazias e casos limite.  
6. Subir logs estruturados (`slf4j` + `logback`) ao invés de `System.out` para ambiente de produção.

---

## Observação final (transparência)
Este guia foi escrito para cobrir **todas** as linhas e conceitos do código que você me mostrou. Se você quiser, eu posso:
- 1) Gerar o `S3Service` com AWS SDK v2 real (ex.: código pronto para colocar suas credenciais/role).  
- 2) Alterar a lógica de identificação do cabeçalho para examinar célula-a-célula (mais robusto).  
- 3) Adicionar exemplos de testes JUnit para `TratamentoDados`.

Diga qual dessas 3 opções prefere que eu faça em seguida e eu já gero o código.  
