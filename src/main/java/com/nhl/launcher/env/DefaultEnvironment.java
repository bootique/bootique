package com.nhl.launcher.env;

public class DefaultEnvironment implements Environment {

	@Override
	public String getProperty(String name) {
		return System.getProperty(name);
	}
}
