package com.nhl.bootique.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class LoggerFactory {

	private Level level;

	public LoggerFactory() {
		this.level = Level.INFO;
	}

	public void setLevel(String level) {
		this.level = Level.toLevel(level, Level.INFO);
	}

	public void configLogger(String loggerName, LoggerContext context) {
		Logger logger = context.getLogger(loggerName);
		logger.setLevel(this.level);
		
		// TODO: appenders
	}

}
