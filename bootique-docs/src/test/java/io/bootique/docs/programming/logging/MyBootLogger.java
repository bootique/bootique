package io.bootique.docs.programming.logging;

import io.bootique.log.BootLogger;

import java.util.function.Supplier;

public class MyBootLogger implements BootLogger {
    @Override
    public void trace(Supplier<String> messageSupplier) {

    }

    @Override
    public void stdout(String message) {

    }

    @Override
    public void stderr(String message) {

    }

    @Override
    public void stderr(String message, Throwable th) {

    }
}
