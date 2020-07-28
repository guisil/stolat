package stolat.mail.content;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageConstants {

    public static final String SUBJECT = "Your birthday albums for %s";
    public static final DateTimeFormatter MONTH_DAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd");
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String START = "Greetings,\n\n";
    private static final String END = "Cheers,\nStoLat";
    private static final String MIDDLE = "Here are your birthday albums for %s:\n\n";
    private static final String RESULTS = "%s\n\n";
    public static final String CONTENT = START + MIDDLE + RESULTS + END;
    private static final String NO_RESULTS_MIDDLE = "Unfortunately you have no birthday albums for %s.\nTry again tomorrow!\n\n";
    public static final String NO_RESULTS_CONTENT = START + NO_RESULTS_MIDDLE + END;
}
