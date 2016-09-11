package io.bootique.terminal;

/**
 * Stores information about the terminal for the current process. If the process is not attached to a real terminal,
 * or we are on Windows and can't query the terminal state, there can still be a Terminal object providing default
 * settings to Bootique.
 *
 * @since 0.20
 */
public interface Terminal {

    /**
     * Returns width of the terminal expressed as character columns.
     *
     * @return width of the terminal expressed as character columns.
     */
    int getColumns();
}
