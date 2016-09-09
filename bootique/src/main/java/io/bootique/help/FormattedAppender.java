package io.bootique.help;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;

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
            println(NO_OFFSET, Collections.emptyList());
        }

        println(NO_OFFSET, Collections.singletonList(name));
    }

    public void printText(String... parts) {
        Collection<String> partsList = parts != null ? asList(parts) : Collections.emptyList();
        println(TEXT_OFFSET, asList(parts));
    }

    public void printText(Collection<String> parts) {
        println(TEXT_OFFSET, parts);
    }

    public void printDescription(String... parts) {
        Collection<String> partsList = parts != null ? asList(parts) : Collections.emptyList();
        println(DESCRIPTION_OFFSET, partsList);
    }

    protected void println(String offset, Collection<String> otherParts) {

        // TODO: wrapping output to not exceed a given length...

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
