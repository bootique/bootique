package io.bootique.unit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class BQInternalInMemoryPrintStream extends PrintStream {

    private PrintStream splitOut;

    public BQInternalInMemoryPrintStream(PrintStream splitOut) {
        super(new ByteArrayOutputStream(), true);
        this.splitOut = splitOut;
    }

    @Override
    public void println(String x) {
        splitOut.println(x);
        super.println(x);
    }

    @Override
    public void println(Object x) {
        splitOut.println(x);
        super.println(x);
    }

    public String toString() {
        return new String(((ByteArrayOutputStream) out).toByteArray(), Charset.forName("UTF-8"));
    }
}

