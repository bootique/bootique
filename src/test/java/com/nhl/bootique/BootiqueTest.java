package com.nhl.bootique;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
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
	public void testCreateInjector_Modules_Instances() {
		Injector i = bootique.modules(new TestModule1(), new TestModule2()).createInjector();
		Set<String> strings = setOf(i, String.class);

		assertTrue(strings.contains("tm1"));
		assertTrue(strings.contains("tm2"));
	}

	@Test
	public void testCreateInjector_Modules_Types() {
		Injector i = bootique.modules(TestModule1.class, TestModule2.class).createInjector();
		Set<String> strings = setOf(i, String.class);

		assertTrue(strings.contains("tm1"));
		assertTrue(strings.contains("tm2"));
	}

	static <T> Set<T> setOf(Injector injector, Class<T> type) {
		return injector.getInstance(Key.get(setOf(type)));
	}

	@SuppressWarnings("unchecked")
	static <T> TypeLiteral<Set<T>> setOf(Class<T> type) {
		return (TypeLiteral<Set<T>>) TypeLiteral.get(Types.setOf(type));
	}

	static class TestModule1 implements Module {

		@Override
		public void configure(Binder binder) {
			Multibinder.newSetBinder(binder, String.class).addBinding().toInstance("tm1");
		}
	}

	static class TestModule2 implements Module {

		@Override
		public void configure(Binder binder) {
			Multibinder.newSetBinder(binder, String.class).addBinding().toInstance("tm2");
		}
	}
}
