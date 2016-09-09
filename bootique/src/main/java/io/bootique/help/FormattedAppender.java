package io.bootique.help;

import java.io.IOException;

/**
 * A helper for building a text help document with consistent formatting. It will include line breaks to separate
 * sections, will provide consistent offsets, text wrapping, etc.
 *
 * @since 0.20
 */
public class FormattedAppender {

    static final String NEWLINE = System.getProperty("line.separator");

    private static final String NO_OFFSET = "";
    private static final String TEXT_OFFSET = "   ";
    private static final String DESCRIPTION_OFFSET = TEXT_OFFSET + "     ";

    private Appendable out;
    private int sectionCount;

    public FormattedAppender(Appendable out) {
        this.out = out;
    }

    public void printSectionName(String name) {

        if (sectionCount++ > 0) {
            println(NO_OFFSET);
        }

        println(name);
    }

    public void printText(String... parts) {
        println(TEXT_OFFSET, parts);
    }

    public void printDescription(String... parts) {
        println(DESCRIPTION_OFFSET, parts);
    }

    protected void println(String offset, String... otherParts) {
        try {
            out.append(offset);
            for (String p : otherParts) {
                out.append(p);
            }
            out.append(NEWLINE);
        } catch (IOException e) {
            throw new RuntimeException("Error printing help", e);
        }
    }
}
