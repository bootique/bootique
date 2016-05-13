package com.nhl.bootique.unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.multibindings.MapBinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.log.DefaultBootLogger;

public class BQInternalTestFactory extends ExternalResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(BQInternalTestFactory.class);

	protected Collection<BQRuntime> runtimes;

	@Override
	protected void after() {

		LOGGER.info("Stopping runtime...");

		Collection<BQRuntime> localRuntimes = this.runtimes;

		if (localRuntimes != null) {
			localRuntimes.forEach(runtime -> {
				try {
					runtime.shutdown();
				} catch (Exception e) {
					// ignore...
				}
			});
		}
	}

	@Override
	protected void before() {
		this.runtimes = new ArrayList<>();
	}

	public Builder newRuntime() {
		return new Builder(runtimes);
	}

	public static class Builder {

		private static final Consumer<Bootique> DO_NOTHING_CONFIGURATOR = bootique -> {
		};

		private Collection<BQRuntime> runtimes;
		private Consumer<Bootique> configurator;
		private Map<String, String> properties;
		private Map<String, String> variables;

		protected Builder(Collection<BQRuntime> runtimes) {
			this.runtimes = runtimes;
			this.properties = new HashMap<>();
			this.variables = new HashMap<>();
			this.configurator = DO_NOTHING_CONFIGURATOR;
		}

		public Builder property(String key, String value) {
			properties.put(key, value);
			return this;
		}

		public Builder var(String key, String value) {
			variables.put(key, value);
			return this;
		}

		public Builder configurator(Consumer<Bootique> configurator) {
			this.configurator = configurator.andThen(Objects.requireNonNull(configurator));
			return this;
		}

		public BQRuntime build(String... args) {

			Consumer<Bootique> localConfigurator = configurator;

			if (!properties.isEmpty()) {

				Consumer<Bootique> propsConfigurator = bootique -> bootique.module(binder -> {
					MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
					properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
				});

				localConfigurator = localConfigurator.andThen(propsConfigurator);
			}

			if (!variables.isEmpty()) {
				Consumer<Bootique> varsConfigurator = bootique -> bootique.module(binder -> {
					MapBinder<String, String> mapBinder = BQCoreModule.contributeVariables(binder);
					variables.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
				});

				localConfigurator = localConfigurator.andThen(varsConfigurator);
			}

			Bootique bootique = Bootique.app(args).bootLogger(new DefaultBootLogger(true));
			localConfigurator.accept(bootique);
			BQRuntime runtime = bootique.createRuntime();

			runtimes.add(runtime);
			return runtime;
		}
	}
}
