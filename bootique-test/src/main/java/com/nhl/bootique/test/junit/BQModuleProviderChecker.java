package com.nhl.bootique.test.junit;

import static java.util.stream.Collectors.counting;
import static org.junit.Assert.fail;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import com.nhl.bootique.BQModuleProvider;

/**
 * A helper class to simplify writing the tests that need to check that a
 * specified {@link BQModuleProvider} is available via ServiceLoader mechanism.
 * This is a useful test as
 * <code>META-INF/services/com.nhl.bootique.BQModuleProvider</code> files can
 * contain typos or omissions. Sample usage:
 * 
 * <pre>
 * &#64;Test
 * public void testPresentInJar() {
 * 	BQModuleProviderChecker.testPresentInJar(MyModuleProvider.class);
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQModuleProviderChecker {

	private Class<? extends BQModuleProvider> expectedProvider;

	public static void testPresentInJar(Class<? extends BQModuleProvider> expectedProvider) {
		new BQModuleProviderChecker(expectedProvider).testPresentInJar();
	}

	protected BQModuleProviderChecker(Class<? extends BQModuleProvider> expectedProvider) {
		this.expectedProvider = Objects.requireNonNull(expectedProvider);
	}

	protected void testPresentInJar() {
		Long c = StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
				.filter(p -> p != null && expectedProvider.equals(p.getClass())).collect(counting());

		switch (c.intValue()) {
		case 0:
			fail("Expected provider '" + expectedProvider.getName() + "' is not found");
			break;

		case 1:
			break;
		default:
			fail("Expected provider '" + expectedProvider.getName() + "' is found more then once: " + c);
			break;
		}
	}
}
