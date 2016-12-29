package io.bootique.help;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A helper for printing text to a fixed-width console that handles text folding and offsets.
 *
 * @since 0.21
 */
public class ConsoleAppender {

    static final String NEWLINE = System.getProperty("line.separator");

    private static final int MIN_LINE_WIDTH = 40;
    private static final Pattern SPACE = Pattern.compile("\\s+");

    private Appendable out;
    private int lineWidth;
    private String offset;

    public ConsoleAppender(Appendable out, int lineWidth) {

        if (lineWidth < MIN_LINE_WIDTH) {
            throw new IllegalArgumentException("Line width is too small. Minimal supported width is " + MIN_LINE_WIDTH);
        }

        this.offset = "";
        this.out = out;
        this.lineWidth = lineWidth;
    }

    protected ConsoleAppender(ConsoleAppender proto) {
        out = proto.out;
        lineWidth = proto.lineWidth;
        offset = proto.offset;

        // do not copy "transient" properties
    }

    private static List<String> asList(String... parts) {
        return parts != null ? Arrays.asList(parts) : Collections.emptyList();
    }

    /**
     * Creates and returns a new appender with base offset equals to default text offset of 6 spaces.
     *
     * @return a new appender with base offset equals to default text offset.
     */
    public ConsoleAppender withOffset() {
        return withOffset(6);
    }

    /**
     * Creates and returns a new appender with the specified base offset.
     *
     * @return a new appender with the specified base offset.
     */
    public ConsoleAppender withOffset(int offset) {
        ConsoleAppender offsetAppender = new ConsoleAppender(this);

        if (offset > 0) {
            StringBuilder padding = new StringBuilder(this.offset);
            for (int i = 0; i < offset; i++) {
                padding.append(" ");
            }

            offsetAppender.offset = padding.toString();
        }

        return offsetAppender;
    }

    public void println() {
        try {
            out.append(NEWLINE);
        } catch (IOException e) {
            throw new RuntimeException("Error printing help", e);
        }
    }

    public void println(String... phrases) {
        println(asList(phrases));
    }

    public void println(Collection<String> phrases) {

        try {
            out.append(this.offset);
            for (String p : phrases) {
                out.append(p);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error printing help", e);
        }

        println();
    }

    public void foldPrintln(String... phrases) {
        foldPrintln(asList(phrases));
    }

    public void foldPrintln(Collection<String> phrases) {

        foldToLines(phrases).forEach(line -> {
            try {
                out.append(this.offset);
                out.append(line);
                out.append(NEWLINE);
            } catch (IOException e) {
                throw new RuntimeException("Error printing help", e);
            }
        });
    }

    protected Collection<String> foldToLines(Collection<String> phrases) {

        int offset = this.offset.length();

        // refuse to fold lines to columns shorter than MIN_WIDTH
        int maxLength = lineWidth - offset < MIN_LINE_WIDTH ? lineWidth - offset : MIN_LINE_WIDTH;

        List<String> folded = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        // must split even shorter strings as they may be combined with the following pieces
        phrases.stream().map(String::trim)
                .map(s -> SPACE.split(s))
                .flatMap(lines -> Arrays.asList(lines).stream())
                .forEach(word -> {

                    String separator = line.length() > 0 ? " " : "";

                    if (line.length() + separator.length() + word.length() <= maxLength) {
                        line.append(separator).append(word);
                    } else {

                        if (word.length() <= maxLength) {
                            folded.add(line.toString());
                            line.setLength(0);
                            line.append(word);
                        } else {

                            // fold long words...

                            // append head to existing line
                            int head = maxLength - separator.length() - line.length();
                            if (head > 0) {
                                line.append(separator).append(word.substring(0, head));
                                folded.add(line.toString());
                                line.setLength(0);
                            }

                            int len = word.length();
                            int start = head;
                            int end = -1;
                            while (start < len) {

                                end = start + maxLength;
                                if (end > len) {
                                    // don't fold yet, there may be more words...
                                    end = len;
                                    line.append(word.substring(start, end));
                                } else {
                                    line.append(word.substring(start, end));
                                    folded.add(line.toString());
                                    line.setLength(0);
                                }

                                start = end;
                            }
                        }
                    }
                });

        // save leftovers
        if (line.length() > 0) {
            folded.add(line.toString());
        }

        return folded;
    }

}
