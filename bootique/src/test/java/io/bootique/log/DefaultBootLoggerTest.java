package io.bootique.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.PrintStream;

import io.bootique.log.DefaultBootLogger;
import org.junit.Before;
import org.junit.Test;

public class DefaultBootLoggerTest {

	private PrintStream mockStdout;
	private PrintStream mockStderr;

	@Before
	public void before() {
		this.mockStdout = mock(PrintStream.class);
		this.mockStderr = mock(PrintStream.class);
	}

	@Test
	public void testStdout() {

		DefaultBootLogger logger = new DefaultBootLogger(true, mockStdout, mockStderr);

		logger.stdout("outmsg");
		verify(mockStdout).println("outmsg");
		verifyZeroInteractions(mockStderr);
	}
	
	@Test
	public void testStderr() {

		DefaultBootLogger logger = new DefaultBootLogger(true, mockStdout, mockStderr);

		logger.stderr("errmsg");
		verify(mockStderr).println("errmsg");
		verifyZeroInteractions(mockStdout);
	}
	
	@Test
	public void testTrace() {

		DefaultBootLogger logger = new DefaultBootLogger(true, mockStdout, mockStderr);

		logger.trace(() -> "mytrace");
		verify(mockStderr).println("mytrace");
		verifyZeroInteractions(mockStdout);
	}
	
	@Test
	public void testNoTrace() {

		DefaultBootLogger logger = new DefaultBootLogger(false, mockStdout, mockStderr);

		logger.trace(() -> "mytrace");
		verifyZeroInteractions(mockStderr);
		verifyZeroInteractions(mockStdout);
	}
}
