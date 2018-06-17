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

import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;

/**
 * Encapsulates tested process STDIN and STDERR streams.
 *
 * @since 0.23
 */
public class TestIO {

    private InMemoryPrintStream stdout;
    private InMemoryPrintStream stderr;
    private boolean trace;

    public static TestIO noTrace() {
        return create(false);
    }

    public static TestIO trace() {
        return create(true);
    }

    private static TestIO create(boolean trace) {
        return new TestIO(new InMemoryPrintStream(System.out), new InMemoryPrintStream(System.err), trace);
    }

    protected TestIO(InMemoryPrintStream stdout, InMemoryPrintStream stderr, boolean trace) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.trace = trace;
    }

    public BootLogger getBootLogger() {
        return new DefaultBootLogger(trace, stdout, stderr);
    }

    public String getStderr() {
        return stderr.toString();
    }

    public String getStdout() {
        return stdout.toString();
    }
}
