package io.bootique.terminal;

/**
 * A pseudo terminal that returns default values for columns, etc.
 *
 * @since 0.20
 */
public class FixedWidthTerminal implements Terminal {

    private int fixedWidth;

    public FixedWidthTerminal(int fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    @Override
    public int getColumns() {
        return fixedWidth;
    }
}
