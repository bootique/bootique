package io.bootique.terminal;

/**
 * A pseudo terminal that returns default values for columns, etc.
 *
 * @since 0.20
 */
public class DefaultTerminal implements Terminal {

    @Override
    public int getColumns() {
        return 80;
    }
}
