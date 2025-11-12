package school.sptech.LogsExtracao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogBase {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    protected static String agora() {
        return LocalDateTime.now().format(FORMATTER);
    }
}