package school.sptech.LogsExtracao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final DateTimeFormatter FORMATTER= DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static String agora() {
        return LocalDateTime.now().format(FORMATTER);
    }

    public static void info(String mensagem) {
        System.out.println("[INFO] [" + agora() + "] " + mensagem);
    }

    public static void sucesso(String mensagem) {
        System.out.println("[SUCESSO] [" + agora() + "] " + mensagem);
    }

    public static void erro(String mensagem) {
        System.err.println("[ERRO] [" + agora() + "] " + mensagem);
    }
}