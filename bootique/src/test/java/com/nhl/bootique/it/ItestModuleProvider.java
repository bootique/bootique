package com.nhl.bootique.it;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;

public class ItestModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new ItestModule();
	}

}
