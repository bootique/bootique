package io.bootique.help;

/**
 * Formats and outputs help information for the current application.
 *
 * @since 0.20
 */
public interface HelpGenerator {

    void append(Appendable out);

    default String generate() {
        StringBuilder out = new StringBuilder();
        append(out);
        return out.toString();
    }
}
