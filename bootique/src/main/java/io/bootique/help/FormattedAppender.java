package io.bootique.help;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A helper for building a text help document with consistent formatting. It will include line breaks to separate
 * sections, will provide consistent offsets, text wrapping, etc.
 *
 * @since 0.20
 */
public class FormattedAppender {

    static final String NEWLINE = System.getProperty("line.separator");

    private static final String NO_OFFSET = "";
    private static final String TEXT_OFFSET = "      ";
    private static final String DESCRIPTION_OFFSET = TEXT_OFFSET + "     ";

    // Enforcing min total width of 40 chars ensures that the option lines do not require folding.
    // So the folding procedure will be limited to description text only...
    private static final int MIN_LINE_WIDTH = 40;

    private static final Pattern SPACE = Pattern.compile("\\s+");

    private Appendable out;
    private int lineWidth;
    private String baseOffset;

    // TODO: get rid of appender state in favor of "withOffset"
    private transient int sectionCount;
    private transient int subsectionCount;

    public FormattedAppender(Appendable out, int lineWidth) {

        if (lineWidth < MIN_LINE_WIDTH) {
            throw new IllegalArgumentException("Line width is too small. Minimal supported width is " + MIN_LINE_WIDTH);
        }

        this.baseOffset = "";
        this.out = out;
        this.lineWidth = lineWidth;
    }

    protected FormattedAppender(FormattedAppender proto) {
        out = proto.out;
        lineWidth = proto.lineWidth;
        baseOffset = proto.baseOffset;

        // do not copy "transient" properties
    }

    private static List<String> asList(String... parts) {
        return parts != null ? Arrays.asList(parts) : Collections.emptyList();
    }

    /**
     * Creates and returns a new appender with base offset equals to default text offset.
     *
     * @return a new appender with base offset equals to default text offset.
     * @since 0.21
     */
    public FormattedAppender withOffset() {
        return withOffset(TEXT_OFFSET.length());
    }

    /**
     * Creates and returns a new appender with the specified base offset.
     *
     * @return a new appender with the specified base offset.
     * @since 0.21
     */
    public FormattedAppender withOffset(int offset) {
        FormattedAppender offsetAppender = new FormattedAppender(this);

        if (offset > 0) {
            StringBuilder padding = new StringBuilder(baseOffset);
            for (int i = 0; i < offset; i++) {
                padding.append(" ");
            }

            offsetAppender.baseOffset = padding.toString();
        }

        return offsetAppender;
    }

    public void printSectionName(String name) {

        // line break between sections
        if (sectionCount++ > 0) {
            subsectionCount = 0;
            println(NO_OFFSET, Collections.emptyList());
        }

        println(NO_OFFSET, Collections.singletonList(name));
    }

    public void printSubsectionHeader(String... parts) {
        printSubsectionHeader(asList(parts));
    }

    public void printSubsectionHeader(Collection<String> parts) {

        // line break between subsections
        if (subsectionCount++ > 0) {
            println(NO_OFFSET, Collections.emptyList());
        }

        println(TEXT_OFFSET, parts);
    }


    public void printText(String... parts) {
        println(TEXT_OFFSET, asList(parts));
    }

    public void printText(Collection<String> parts) {
        println(TEXT_OFFSET, parts);
    }

    public void printDescription(String... parts) {
        foldWithOffset(DESCRIPTION_OFFSET.length(), parts)
                .forEach(s -> println(DESCRIPTION_OFFSET, Collections.singleton(s)));
    }

    public void println() {
        try {
            out.append(NEWLINE);
        } catch (IOException e) {
            throw new RuntimeException("Error printing help", e);
        }
    }

    protected void println(String offset, Collection<String> parts) {

        try {
            out.append(baseOffset);
            out.append(offset);
            for (String p : parts) {
                out.append(p);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error printing help", e);
        }

        println();
    }

    protected Collection<String> foldWithOffset(int offset, String... parts) {

        offset += baseOffset.length();

        if (offset > DESCRIPTION_OFFSET.length()) {
            throw new IllegalArgumentException("Offset is too big: " + offset
                    + ". Can't fit the text in remaining space.");
        }

        int maxLength = lineWidth - offset;

        List<String> folded = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        // must split even shorter strings as they may be combined with the following pieces
        FormattedAppender.asList(parts).stream().map(String::trim)
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
