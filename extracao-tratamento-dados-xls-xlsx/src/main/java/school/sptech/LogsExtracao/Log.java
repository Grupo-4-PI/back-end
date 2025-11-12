package school.sptech.LogsExtracao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log extends LogBase {

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