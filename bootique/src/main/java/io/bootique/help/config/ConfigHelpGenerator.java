package io.bootique.help.config;

/**
 * @since 0.21
 */
public interface ConfigHelpGenerator {

    void append(Appendable out);

    default String generate() {
        StringBuilder out = new StringBuilder();
        append(out);
        return out.toString();
    }
}
