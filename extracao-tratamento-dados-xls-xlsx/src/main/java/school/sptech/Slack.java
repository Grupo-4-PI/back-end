package school.sptech;

import org.json.JSONObject;
import school.sptech.LogsExtracao.Log;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Slack {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = System.getenv("SLACK_WEBHOOK_URL");

    public static void enviarMensagem(String mensagem) {
        System.out.println("--- [DEBUG SLACK] Iniciando tentativa de envio ---");

        if (URL == null) {
            System.err.println("[DEBUG SLACK] ERRO: A variável URL está NULL.");
            Log.erro("Slack URL é null");
            return;
        }

        System.out.println("[DEBUG SLACK] URL carregada: [" + URL + "]");

        if (URL.isBlank()) {
            System.err.println("[DEBUG SLACK] ERRO: A URL está vazia (blank).");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("text", mensagem);
            String jsonString = json.toString();

            System.out.println("[DEBUG SLACK] Payload JSON: " + jsonString);

            HttpRequest request = HttpRequest.newBuilder(URI.create(URL.trim()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[DEBUG SLACK] Código de Status HTTP: " + response.statusCode());
            System.out.println("[DEBUG SLACK] Corpo da Resposta: " + response.body());

            if (response.statusCode() != 200) {
                String erroMsg = "Slack rejeitou a mensagem. Status: " + response.statusCode();
                System.err.println(erroMsg);
                Log.erro(erroMsg);
            } else {
                System.out.println("[DEBUG SLACK] Mensagem enviada com sucesso!");
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[DEBUG SLACK] EXCEÇÃO DISPARADA:");
            e.printStackTrace();
            Log.erro("Erro Java ao enviar para Slack: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[DEBUG SLACK] Erro na URL (provavelmente caractere inválido): " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("--- [DEBUG SLACK] Fim da tentativa ---");
    }
}