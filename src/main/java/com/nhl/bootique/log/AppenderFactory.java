package com.nhl.bootique.log;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public class AppenderFactory {

	private String logFormat;

	public AppenderFactory() {
		this.logFormat = "%-5p [%d{ISO8601,UTC}] %thread %c{20}: %m%n%rEx";
	}

	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}

	public Appender<ILoggingEvent> createAppender(LoggerContext context) {

		ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
		appender.setName("console");
		appender.setContext(context);
		appender.setTarget("System.out");

		LayoutWrappingEncoder<ILoggingEvent> layoutEncoder = new LayoutWrappingEncoder<>();
		layoutEncoder.setLayout(createLayout(context));
		appender.setEncoder(layoutEncoder);

		appender.start();

		return asAsync(appender);
	}

	protected PatternLayout createLayout(LoggerContext context) {
		PatternLayout layout = new PatternLayout();
		layout.setPattern(logFormat);
		layout.setContext(context);

		layout.start();
		return layout;
	}

	protected Appender<ILoggingEvent> asAsync(Appender<ILoggingEvent> appender) {
		return asAsync(appender, appender.getContext());
	}

	protected Appender<ILoggingEvent> asAsync(Appender<ILoggingEvent> appender, Context context) {
		final AsyncAppender asyncAppender = new AsyncAppender();
		asyncAppender.setIncludeCallerData(false);
		asyncAppender.setQueueSize(AsyncAppenderBase.DEFAULT_QUEUE_SIZE);
		asyncAppender.setDiscardingThreshold(-1);
		asyncAppender.setContext(context);
		asyncAppender.setName(appender.getName());
		asyncAppender.addAppender(appender);
		asyncAppender.start();
		return asyncAppender;
	}
}
