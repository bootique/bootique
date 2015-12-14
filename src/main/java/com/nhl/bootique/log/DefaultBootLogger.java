package com.nhl.bootique.log;

public class DefaultBootLogger implements BootLogger {

	@Override
	public void stderr(String message) {
		System.err.println(message);
	}

	@Override
	public void stdout(String message) {
		System.out.println(message);
	}
}
