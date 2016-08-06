package io.bootique;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import io.bootique.it.ItestModuleProvider;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import io.bootique.annotation.Args;

public class BootiqueIT {

	private String[] args = new String[] { "a", "b", "c" };

	@Test
	public void testAutoLoadedProviders() {
		Collection<BQModuleProvider> autoLoaded = Bootique.app(args).autoLoadedProviders();

		assertEquals(1, autoLoaded.size());
		autoLoaded.forEach(m -> assertTrue(m instanceof ItestModuleProvider));
	}

	@Test
	public void testCreateInjector() {
		Injector i = Bootique.app(args).createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(this.args, args);
	}
	
	@Test
	public void testApp_Collection() {
		Injector i = Bootique.app(asList(args)).createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertArrayEquals(this.args, args);
	}

	@Test
	public void testCreateInjector_Overrides() {
		Injector i = Bootique.app(args).override(BQCoreModule.class).with(M0.class).createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(M0.ARGS, args);
	}

	@Test
	public void testCreateInjector_Overrides_Multi_Level() {
		Injector i = Bootique.app(args).override(BQCoreModule.class).with(M0.class).override(M0.class).with(M1.class)
				.createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(M1.ARGS, args);
	}

	@Test
	public void testCreateInjector_OverridesWithProvider() {
		BQModuleProvider provider = new BQModuleProvider() {

			@Override
			public Module module() {
				return new M0();
			}

			@Override
			public Collection<Class<? extends Module>> overrides() {
				return Collections.singleton(BQCoreModule.class);
			}
		};

		Injector i = Bootique.app(args).module(provider).createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(M0.ARGS, args);
	}

	static class M0 implements Module {

		static String[] ARGS = { "1", "2", "3" };

		@Override
		public void configure(Binder binder) {
			binder.bind(String[].class).annotatedWith(Args.class).toInstance(ARGS);
		}
	}

	static class M1 implements Module {

		static String[] ARGS = { "x", "y", "z" };

		@Override
		public void configure(Binder binder) {
			binder.bind(String[].class).annotatedWith(Args.class).toInstance(ARGS);
		}
	}
}
