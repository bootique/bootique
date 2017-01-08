package io.bootique.help;

import java.util.Collection;

import static java.util.Arrays.asList;

/*
 * A helper for building a text help document with consistent formatting. It will include line breaks to separate
 * sections, will provide consistent offsets, text wrapping, etc.
 *
 * @since 0.21
 */
public class HelpAppender {

    private ConsoleAppender rootAppender;
    private ConsoleAppender offsetAppender;
    private ConsoleAppender doubleOffsetAppender;

    private int sectionCount;
    private int subsectionCount;

    public HelpAppender(ConsoleAppender appender) {
        this.rootAppender = appender;
    }

    public ConsoleAppender getAppender() {
        return rootAppender;
    }

    public void printSectionName(String name) {

        // line break between sections
        if (sectionCount++ > 0) {
            subsectionCount = 0;
            rootAppender.println();
        }

        rootAppender.println(name);
    }

    public void printSubsectionHeader(String... parts) {
        printSubsectionHeader(asList(parts));
    }

    public void printSubsectionHeader(Collection<String> parts) {

        // line break between subsections
        if (subsectionCount++ > 0) {
            rootAppender.println();
        }

        getOrCreateOffsetAppender().foldPrintln(parts);
    }

    public void printText(String... parts) {
        getOrCreateOffsetAppender().println(parts);
    }

    public void printDescription(String... parts) {
        getOrCreateDoubleOffsetAppender().foldPrintln(parts);
    }

    public void println() {
        rootAppender.println();
    }

    private ConsoleAppender getOrCreateOffsetAppender() {
        if (this.offsetAppender == null) {
            this.offsetAppender = rootAppender.withOffset(6);
        }

        return offsetAppender;
    }

    private ConsoleAppender getOrCreateDoubleOffsetAppender() {
        if (this.doubleOffsetAppender == null) {
            // "5" is the magic number used for decsription offset. It seems to look best when description is printed
            // under the options
            this.doubleOffsetAppender = getOrCreateOffsetAppender().withOffset(5);
        }

        return doubleOffsetAppender;
    }
}
