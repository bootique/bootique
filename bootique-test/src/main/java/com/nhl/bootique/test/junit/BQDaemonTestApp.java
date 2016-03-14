package com.nhl.bootique.test.junit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import com.google.inject.multibindings.MapBinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.test.BQDaemonTestRuntime;

/**
 * Manages a "daemon" Bootique stack within a lifecycle of the a JUnit test.
 * This allows to start background servers so that tests can execute requests
 * against them, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * 
 * <pre>
 * public class MyTest {
 * 
 * 	&#64;Rule
 * 	public BQDaemonTestApp testApp = new BQDaemonTestApp();
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQDaemonTestApp extends ExternalResource {

	private static final Consumer<Bootique> DO_NOTHING_CONFIGURATOR = bootique -> {
	};

	private static final Function<BQRuntime, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

	private Function<BQRuntime, Boolean> startupCheck;
	private Consumer<Bootique> configurator;
	private Map<String, String> properties;
	private BQDaemonTestRuntime runtime;

	public BQDaemonTestApp() {
		this.properties = new HashMap<>();
		this.configurator = DO_NOTHING_CONFIGURATOR;
		this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
	}

	@Override
	protected void after() {
		this.configurator = DO_NOTHING_CONFIGURATOR;
		this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
		this.properties.clear();
		shutdownRuntime();
	}

	protected void shutdownRuntime() {
		BQDaemonTestRuntime localRuntime = this.runtime;
		if (localRuntime != null) {
			this.runtime = null;
			localRuntime.stop();
		}
	}

	public BQDaemonTestApp property(String key, String value) {
		properties.put(key, value);
		return this;
	}

	public BQDaemonTestApp configurator(Consumer<Bootique> configurator) {
		this.configurator = Objects.requireNonNull(configurator);
		return this;
	}

	public BQDaemonTestApp startupCheck(Function<BQRuntime, Boolean> startupCheck) {
		this.startupCheck = Objects.requireNonNull(startupCheck);
		return this;
	}

	/**
	 * Starts the test app in a different thread.
	 * 
	 * @param args
	 *            String[] emulating command-line arguments passed to a Java
	 *            app.
	 */
	public void start(String... args) {

		// reset any previously managed runtime
		shutdownRuntime();

		Consumer<Bootique> localConfigurator = configurator;

		if (!properties.isEmpty()) {

			Consumer<Bootique> propsConfigurator = bootique -> bootique.module(binder -> {
				MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
				properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
			});

			localConfigurator = localConfigurator.andThen(propsConfigurator);
		}

		this.runtime = new BQDaemonTestRuntime(localConfigurator, startupCheck);
		runtime.start(5, TimeUnit.SECONDS, args);
	}
}
