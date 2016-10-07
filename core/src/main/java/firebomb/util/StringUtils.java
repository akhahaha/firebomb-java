package firebomb.util;

public class StringUtils {
    public static String join(String delimiter, String... strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length - 1; i++) {
            builder.append(strings[i]);
            builder.append(delimiter);
        }

        builder.append(strings[strings.length - 1]);

        return builder.toString();
    }

    public static String path(String... nodes) {
        return join("/", nodes);
    }
}
