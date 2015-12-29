package com.nhl.bootique.shutdown;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

public class DefaultShutdownManagerIT {

	private AutoCloseable mockCloseable1;
	private AutoCloseable mockCloseable2;

	@Before
	public void before() {

		this.mockCloseable1 = mock(AutoCloseable.class);
		this.mockCloseable2 = mock(AutoCloseable.class);
	}

	@Test
	public void testShutdown() throws Exception {
		Duration timeout = Duration.ofMillis(10000l);
		DefaultShutdownManager shutdownManager = new DefaultShutdownManager(timeout);

		shutdownManager.addShutdownHook(mockCloseable1);
		shutdownManager.addShutdownHook(mockCloseable2);

		shutdownManager.shutdown();

		verify(mockCloseable1).close();
		verify(mockCloseable2).close();
	}

	@Test
	public void testShutdown_Unresponsive_Timeout() throws Exception {
		Duration timeout = Duration.ofMillis(500l);
		DefaultShutdownManager shutdownManager = new DefaultShutdownManager(timeout);

		doAnswer(i -> {
			while (true) {
				// spinning...
			}
		}).when(mockCloseable2).close();

		shutdownManager.addShutdownHook(mockCloseable1);
		shutdownManager.addShutdownHook(mockCloseable2);

		long t0 = System.currentTimeMillis();
		shutdownManager.shutdown();

		long t1 = System.currentTimeMillis();

		verify(mockCloseable1).close();
		verify(mockCloseable2).close();

		assertTrue(t1 - t0 >= timeout.toMillis());

		// too optimistic??
		assertTrue(t1 - t0 < timeout.toMillis() + 1000);
	}
}
