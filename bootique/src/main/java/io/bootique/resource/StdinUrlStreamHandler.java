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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

class StdinUrlStreamHandler extends URLStreamHandler {

    private final String contentType;

    public StdinUrlStreamHandler(String contentType) {
        this.contentType = contentType;
    }

    @Override
    protected URLConnection openConnection(URL url) {
        // Can consume only if STDIN is not an interactive console
        return System.console() == null
                ? new InputStreamUrlConnection(url, System.in, contentType)
                : new EmptyUrlConnection(url, contentType);
    }

    static class EmptyUrlConnection extends URLConnection {

        private final String contentType;

        public EmptyUrlConnection(URL url, String contentType) {
            super(url);
            this.contentType = contentType;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public void connect() throws IOException {
            // nothing to connect
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    static class InputStreamUrlConnection extends URLConnection {

        private final InputStream in;
        private final String contentType;

        public InputStreamUrlConnection(URL url, InputStream in, String contentType) {
            super(url);
            this.in = in;
            this.contentType = contentType;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public void connect() throws IOException {
            // do nothing ... we already have a stream
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new NonClosableInputStream(in);
        }
    }

    static class NonClosableInputStream extends InputStream {
        private final InputStream delegate;

        public NonClosableInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public void close() {
            // Do not close the underlying stream (e.g., System.in should not be closed)
        }
    }
}
