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

import java.io.PrintStream;
import java.util.function.Supplier;

public class DefaultBootLogger implements BootLogger {

	private boolean trace;
	private PrintStream stdout;
	private PrintStream stderr;

	public DefaultBootLogger(boolean trace) {
		this(trace, System.out, System.err);
	}

	public DefaultBootLogger(boolean trace, PrintStream stdout, PrintStream stderr) {
		this.trace = trace;
		this.stderr = stderr;
		this.stdout = stdout;
	}

	@Override
	public void trace(Supplier<String> messageSupplier) {
		if (trace) {
			stderr(messageSupplier.get());
		}
	}

	@Override
	public void stdout(String message) {
		stdout.println(message);
	}

	@Override
	public void stderr(String message) {
		stderr.println(message);
	}

	@Override
	public void stderr(String message, Throwable th) {
		stderr(message);

		if(th != null) {
			th.printStackTrace(stderr);
		}
	}
}
