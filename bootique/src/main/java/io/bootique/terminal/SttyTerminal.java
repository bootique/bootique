package io.bootique.terminal;

import io.bootique.log.BootLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A terminal based on UNIX 'tput' command.
 *
 * @since 0.20
 */
public class SttyTerminal extends ExternalCommandTerminal {

    // TODO: is "columns" a localizable word?
    private static final Pattern COLUMNS_PATTERN = Pattern.compile("([0-9]+)\\scolumns;");

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
        // on OSX...
        // columns information is printed on line 1 and looks like this:
        //
        // speed 9600 baud; 43 rows; 141 columns;
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

        Matcher matcher = COLUMNS_PATTERN.matcher(line1);

        if (!matcher.find()) {
            logger.trace(() ->
                    "Unexpected output for '"
                            + ExternalCommandTerminal.toString(getColumnsCommand())
                            + "' command: "
                            + line1);
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            logger.trace(() ->
                    "Unexpected output for '"
                            + ExternalCommandTerminal.toString(getColumnsCommand())
                            + "' command: "
                            + line1);
            return null;
        }
    }
}
