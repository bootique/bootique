package io.bootique.test.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;

import io.bootique.test.junit.BQDaemonTestFactory;
import org.junit.Rule;
import org.junit.Test;

import io.bootique.test.BQDaemonTestRuntime;

public class BQDaemonTestFactoryIT {

	@Rule
	public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();

	@Test
	public void test1() {
		BQDaemonTestRuntime r1 = testFactory.newRuntime().startupAndWaitCheck().start("a1", "a2");
		assertArrayEquals(new String[] { "a1", "a2" }, r1.getRuntime().getArgs());

		BQDaemonTestRuntime r2 = testFactory.newRuntime().startupAndWaitCheck().start("b1", "b2");
		assertNotSame(r1, r2);
		assertArrayEquals(new String[] { "b1", "b2" }, r2.getRuntime().getArgs());
	}
}
