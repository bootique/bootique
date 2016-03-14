package com.nhl.bootique.test.junit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import com.google.inject.multibindings.MapBinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.test.BQTestRuntime;

/**
 * Provides an object that manages a simple Bootique stack within a lifecycle of
 * the a JUnit test. It doesn't run any commands by default and is usually used
 * for accessing initialized standard services, such as
 * {@link ConfigurationFactory}, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * 
 * <pre>
 * public class MyTest {
 * 
 * 	&#64;Rule
 * 	public BQTestApp testApp = new BQTestApp();
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQTestApp extends ExternalResource {

	private Consumer<Bootique> configurator;
	private Map<String, String> properties;
	private BQRuntime runtime;

	public BQTestApp() {
		this.properties = new HashMap<>();
	}

	@Override
	protected void after() {
		this.configurator = null;
		this.properties.clear();
		shutdownRuntime();
	}

	protected void shutdownRuntime() {
		BQRuntime localRuntime = this.runtime;
		if (localRuntime != null) {
			this.runtime = null;
			localRuntime.shutdown();
		}
	}

	public BQTestApp property(String key, String value) {
		properties.put(key, value);
		return this;
	}

	public BQTestApp configurator(Consumer<Bootique> configurator) {
		this.configurator = configurator;
		return this;
	}

	public BQRuntime createRuntime(String... args) {

		// reset any previously managed runtime
		shutdownRuntime();

		Consumer<Bootique> localConfigurator = bootique -> bootique.module(binder -> {
			MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
			properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
		});

		if (configurator != null) {
			localConfigurator = localConfigurator.andThen(configurator);
		}

		this.runtime = new BQTestRuntime(localConfigurator).createRuntime(args);
		return runtime;
	}
}
