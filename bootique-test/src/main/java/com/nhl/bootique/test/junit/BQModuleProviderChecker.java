package com.nhl.bootique.test.junit;

import static java.util.stream.Collectors.counting;
import static org.junit.Assert.assertEquals;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import com.nhl.bootique.BQModuleProvider;

/**
 * A helper class to simplify writing the tests that need to check that a
 * specified {@link BQModuleProvider} is available via ServiceLoader mechanism.
 * This is a useful test as
 * <code>META-INF/services/com.nhl.bootique.BQModuleProvider</code> files can
 * contain typos or omissions.
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
		long c = StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
				.filter(p -> p != null && expectedProvider.isAssignableFrom(p.getClass())).collect(counting());
		assertEquals("Expected provider " + expectedProvider.getName() + " is not found", 1, c);
	}
}
