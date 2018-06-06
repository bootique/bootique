/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class InMemoryPrintStream extends PrintStream {

	private PrintStream splitOut;

	public InMemoryPrintStream(PrintStream splitOut) {
		super(new ByteArrayOutputStream(), true);
		this.splitOut = splitOut;
	}

	@Override
	public void println(String x) {
		splitOut.println(x);
		super.println(x);
	}

	@Override
	public void println(Object x) {
		splitOut.println(x);
		super.println(x);
	}

	public String toString() {
		return new String(((ByteArrayOutputStream) out).toByteArray(), Charset.forName("UTF-8"));
	}
}
