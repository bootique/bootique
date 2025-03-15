/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class DefaultBootLoggerTest {

	private PrintStream mockStdout;
	private PrintStream mockStderr;

	@BeforeEach
	public void before() {
		this.mockStdout = mock(PrintStream.class);
		this.mockStderr = mock(PrintStream.class);
	}

	@Test
    public void stdout() {

		DefaultBootLogger logger = new DefaultBootLogger(true, mockStdout, mockStderr);

		logger.stdout("outmsg");
		verify(mockStdout).println("outmsg");
		verifyNoInteractions(mockStderr);
	}
	
	@Test
    public void stderr() {

		DefaultBootLogger logger = new DefaultBootLogger(true, mockStdout, mockStderr);

		logger.stderr("errmsg");
		verify(mockStderr).println("errmsg");
		verifyNoInteractions(mockStdout);
	}
	
	@Test
	public void trace() {

		DefaultBootLogger logger = new DefaultBootLogger(true, mockStdout, mockStderr);

		logger.trace(() -> "mytrace");
		verify(mockStderr).println("mytrace");
		verifyNoInteractions(mockStdout);
	}
	
	@Test
	public void noTrace() {

		DefaultBootLogger logger = new DefaultBootLogger(false, mockStdout, mockStderr);

		logger.trace(() -> "mytrace");
		verifyNoInteractions(mockStderr);
		verifyNoInteractions(mockStdout);
	}
}
