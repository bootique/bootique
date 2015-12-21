package com.nhl.bootique.it;

import java.util.Optional;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;

public class ItestModuleProvider implements BQModuleProvider {

	public static Class<? extends Module> REPLACES;

	@Override
	public Module module() {
		return new ItestModule();
	}

	@Override
	public Optional<Class<? extends Module>> replaces() {
		return Optional.ofNullable(REPLACES);
	}
}
