package school.sptech;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        ExtrairDadosPlanilha extrator = new ExtrairDadosPlanilha();
        String caminhoDoArquivo = "C:/Users/vvent/Downloads/airwise-base-de-dados-2024.xlsx";

        System.out.println("Iniciando o programa...");

        Map<String, List<Dados>> dadosProntos = extrator.extrairETratarDados(caminhoDoArquivo);

        if (dadosProntos != null) {
            System.out.println("\n--- PROCESSO CONCLUÍDO COM SUCESSO ---");
            System.out.println("Resumo do tratamento:");

            List<String> nomesDasAbas = new ArrayList<>(dadosProntos.keySet());
            for (int i = 0; i < nomesDasAbas.size(); i++) {
                String nomeAba = nomesDasAbas.get(i);
                int quantidade = dadosProntos.get(nomeAba).size();
                System.out.println("  - Aba '" + nomeAba + "' gerou " + quantidade + " registros válidos.");
            }

            System.out.println("\nOs dados agora estão tratados e prontos para a próxima etapa.");

            if (!nomesDasAbas.isEmpty()) {
                String primeiraAba = nomesDasAbas.get(0);
                List<Dados> linha = dadosProntos.get(primeiraAba);

                if (!linha.isEmpty()) {
                    System.out.println("\nExibindo registros válidos da aba '" + primeiraAba + "':");
                    for (int i = 0; i < linha.size(); i++) {
                        Dados rec = linha.get(i);
                        System.out.println("  " + rec);
                    }
                }
            }
        } else {
            System.out.println("\n--- O PROCESSO FALHOU ---");
            System.out.println("Verifique os erros mostrados acima no console.");
        }
    }
}