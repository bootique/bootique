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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultBootLoggerTest {

    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private PrintStream stdout;
    private PrintStream stderr;

    @BeforeEach
    public void before() {
        this.out = new ByteArrayOutputStream();
        this.err = new ByteArrayOutputStream();
        this.stdout = new PrintStream(out);
        this.stderr = new PrintStream(err);
    }

    @Test
    public void stdout() {

        DefaultBootLogger logger = new DefaultBootLogger(true, stdout, stderr);

        logger.stdout("outmsg");
        checkStreams("outmsg", null);
    }

    @Test
    public void stderr() {

        DefaultBootLogger logger = new DefaultBootLogger(true, stdout, stderr);

        logger.stderr("errmsg");
        checkStreams(null, "errmsg");
    }

    @Test
    public void trace() {
        DefaultBootLogger logger = new DefaultBootLogger(true, stdout, stderr);

        logger.trace(() -> "mytrace");
        checkStreams(null, "mytrace");
    }

    @Test
    public void noTrace() {
        DefaultBootLogger logger = new DefaultBootLogger(false, stdout, stderr);

        logger.trace(() -> "mytrace");
        checkStreams(null, null);
    }

    private void checkStreams(String expectedStdout, String expectedStderr) {
        stdout.flush();
        stderr.flush();

        if (expectedStdout == null) {
            assertEquals(0, out.size());
        } else {
            assertEquals(expectedStdout + System.lineSeparator(), out.toString());
        }

        if (expectedStderr == null) {
            assertEquals(0, err.size());
        } else {
            assertEquals(expectedStderr + System.lineSeparator(), err.toString());
        }
    }
}
