package io.bootique.terminal;

import io.bootique.log.BootLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A terminal based on UNIX 'stty -a' command.
 *
 * @since 0.20
 */
public class SttyTerminal extends ExternalCommandTerminal {

    // OSX:     speed 9600 baud; 43 rows; 141 columns;
    // Linux:   speed 9600 baud; rows 40; columns 148; line = 0;
    private static final Pattern COLUMNS_PATTERN_OSX = Pattern.compile("([0-9]+)\\scolumns;");
    private static final Pattern COLUMNS_PATTERN_LINUX = Pattern.compile("columns\\s([0-9]+);");

    // "stty" seems to be present in /bin on OSX, CentOS, Ubuntu...
    private static final String[] STTY_COMMAND = new String[]{"/bin/stty", "-a"};

    public SttyTerminal(Terminal failoverTerminal, BootLogger logger) {
        super(failoverTerminal, logger);
    }

    @Override
    protected String[] getColumnsCommand() {
        return STTY_COMMAND;
    }

    @Override
    protected Integer parseColumns(BufferedReader commandOutput) {

        //
        // columns information is printed on line 1, so ignoring the rest of it...
        //

        String line1;
        try {
            line1 = commandOutput.readLine();
        } catch (IOException e) {
            logger.trace(() -> "Error reading output for '"
                    + ExternalCommandTerminal.toString(getColumnsCommand())
                    + "' command: "
                    + e.getMessage());
            return null;
        }

        return parseLine(line1);
    }

    protected Integer parseLine(String line) {
        return parseLine(line, COLUMNS_PATTERN_LINUX, COLUMNS_PATTERN_OSX);
    }

    protected Integer parseLine(String line, Pattern... patterns) {
        Matcher matcher;

        for (Pattern pattern : patterns) {
            matcher = pattern.matcher(line);

            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    logger.trace(() ->
                            "Unexpected output for '"
                                    + ExternalCommandTerminal.toString(getColumnsCommand())
                                    + "' command: "
                                    + line);
                    return null;
                }
            }
        }

        logger.trace(() ->
                "Unexpected output for '"
                        + ExternalCommandTerminal.toString(getColumnsCommand())
                        + "' command: "
                        + line);
        return null;
    }
}
