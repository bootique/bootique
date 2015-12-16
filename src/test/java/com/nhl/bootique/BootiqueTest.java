package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;

public class BootiqueTest {

	private Bootique bootique;

	@Before
	public void before() {
		this.bootique = Bootique.app(new String[0]);
	}

	@Test
	public void testCreateInjector_Bundles() {
		Injector i = bootique.bundles(TestBundle1.class, TestBundle2.class).createInjector();
		Set<String> prefixes = i.getInstance(Key.get(setOf(String.class)));
		assertTrue(prefixes.contains("testbundle1"));
		assertTrue(prefixes.contains("testbundle2"));
	}

	@Test
	public void testCreateInjector_Bundle_DefaultPrefix() {
		Injector i = bootique.bundle(TestBundle.class).createInjector();
		assertEquals("test", i.getInstance(String.class));
	}

	@Test
	public void testCreateInjector_Bundle_EmptyPrefix() {
		Injector i = bootique.bundle(TestBundle.class, "").createInjector();
		assertEquals("", i.getInstance(String.class));
	}

	@Test
	public void testCreateInjector_Bundle_Prefix() {
		Injector i = bootique.bundle(TestBundle.class, "some.prefix").createInjector();
		assertEquals("some.prefix", i.getInstance(String.class));
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeLiteral<Set<T>> setOf(Class<T> type) {
		return (TypeLiteral<Set<T>>) TypeLiteral.get(Types.setOf(type));
	}

	static class TestBundle implements BQBundle {

		@Override
		public Module module(String configPrefix) {
			return b -> b.bind(String.class).toInstance(configPrefix);
		}
	}

	static class TestBundle1 implements BQBundle {

		@Override
		public Module module(String configPrefix) {
			return b -> Multibinder.newSetBinder(b, String.class).addBinding().toInstance(configPrefix);
		}
	}

	static class TestBundle2 implements BQBundle {

		@Override
		public Module module(String configPrefix) {
			return b -> Multibinder.newSetBinder(b, String.class).addBinding().toInstance(configPrefix);
		}
	}
}
