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
package io.bootique.resource;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class InputStreamUrlConnectionTest {

    private static URL createTestUrl() throws MalformedURLException {
        return new File("/test/input").toURI().toURL();
    }

    @Test
    public void getInputStream_ImmediateData() throws IOException {
        String testData = "Hello, World!";
        InputStream in = new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8));
        StdinUrlStreamHandler.InputStreamUrlConnection connection = new StdinUrlStreamHandler.InputStreamUrlConnection(
                createTestUrl(),
                in,
                "text/plain"
        );

        try (InputStream result = connection.getInputStream()) {
            String content = new String(result.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(testData, content);
        }
    }

    @Test
    public void getInputStream_EmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        StdinUrlStreamHandler.InputStreamUrlConnection connection = new StdinUrlStreamHandler.InputStreamUrlConnection(
                createTestUrl(),
                in,
                "text/plain"
        );

        try (InputStream result = connection.getInputStream()) {
            byte[] content = result.readAllBytes();
            assertEquals(0, content.length);
        }
    }

    @Test
    public void getContentType() throws IOException {
        InputStream in = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        StdinUrlStreamHandler.InputStreamUrlConnection connection = new StdinUrlStreamHandler.InputStreamUrlConnection(
                createTestUrl(),
                in,
                "application/json"
        );

        assertEquals("application/json", connection.getContentType());
    }

    @Test
    public void connect() throws IOException {
        InputStream in = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        StdinUrlStreamHandler.InputStreamUrlConnection connection = new StdinUrlStreamHandler.InputStreamUrlConnection(
                createTestUrl(),
                in,
                "text/plain"
        );

        // Should not throw
        connection.connect();
    }

    @Test
    public void getInputStream_DoesNotCloseUnderlyingStream() throws IOException {
        String testData = "Test data";

        // Custom InputStream that tracks if it has been closed
        class TrackableInputStream extends ByteArrayInputStream {
            private boolean closed = false;

            public TrackableInputStream(byte[] data) {
                super(data);
            }

            @Override
            public void close() throws IOException {
                super.close();
                closed = true;
            }

            public boolean isClosed() {
                return closed;
            }
        }

        TrackableInputStream trackableStream = new TrackableInputStream(
                testData.getBytes(StandardCharsets.UTF_8));

        StdinUrlStreamHandler.InputStreamUrlConnection connection = new StdinUrlStreamHandler.InputStreamUrlConnection(
                createTestUrl(),
                trackableStream,
                "text/plain"
        );

        // Read from the connection
        try (InputStream result = connection.getInputStream()) {
            byte[] content = result.readAllBytes();
            assertEquals(testData, new String(content, StandardCharsets.UTF_8));
        }

        // Verify the underlying stream was NOT closed
        assertFalse(trackableStream.isClosed(),
                "Underlying stream should remain open (e.g., for STDIN)");

        // Verify we can still interact with the underlying stream
        assertEquals(-1, trackableStream.read(),
                "Stream should be at EOF but still open");
    }
}
