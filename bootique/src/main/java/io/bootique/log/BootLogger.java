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

import java.util.function.Supplier;

/**
 * A special logger that can be used by services participating in the boot
 * sequence before the logging subsystem is started. It would usually log to
 * STDOUT/STDERR.
 */
public interface BootLogger {

	/**
	 * Outputs the message to STDERR only if BootLogger is in a trace mode. Uses
	 * message supplier, so that the actual message production can be
	 * conditionally skipped if tracing is not enabled.
	 * 
	 * @param messageSupplier
	 *            a supplier of a String message.
	 */
	void trace(Supplier<String> messageSupplier);

	void stdout(String message);

	void stderr(String message);

	void stderr(String message, Throwable th);
}
