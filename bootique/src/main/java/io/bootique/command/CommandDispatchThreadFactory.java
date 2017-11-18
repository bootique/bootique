package io.bootique.command;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 0.25
 */
public class CommandDispatchThreadFactory implements ThreadFactory {

    private AtomicInteger counter = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("bootique-command-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
    }
}
