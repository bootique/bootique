package com.nhl.bootique.test;

import java.util.function.Consumer;

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;

/**
 * A base class of test "shells" that allow to run various modules in Bootique
 * environment.
 * 
 * @since 0.14
 */
public abstract class BQTestRuntime {

	private InMemoryPrintStream stdout;
	private InMemoryPrintStream stderr;
	private Consumer<Bootique> configurator;

	public BQTestRuntime(Consumer<Bootique> configurator) {
		this.stdout = new InMemoryPrintStream(System.out);
		this.stderr = new InMemoryPrintStream(System.err);
		this.configurator = configurator;
	}

	protected BootLogger createBootLogger() {
		return new DefaultBootLogger(true, stdout, stderr);
	}

	public String getStdout() {
		return stdout.toString();
	}

	public String getStderr() {
		return stderr.toString();
	}

	protected BQRuntime createRuntime(String... args) {
		Bootique bootique = Bootique.app(args).bootLogger(createBootLogger());
		configurator.accept(bootique);
		return bootique.createRuntime();
	}
}
