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

package io.bootique.di;

/**
 * A runtime exception thrown on DI misconfiguration.
 */
public class DIRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 396131653561690312L;

    private InjectionTraceElement[] injectionTrace = {};

    /**
     * Creates new <code>DIRuntimeException</code> without detail message.
     */
    public DIRuntimeException() {
    }

    /**
     * Constructs an exception with the specified message with an optional list
     * of message formatting arguments. Message formatting rules follow
     * "String.format(..)" conventions.
     */
    public DIRuntimeException(String messageFormat, Object... messageArgs) {
        super(String.format(messageFormat, messageArgs));
    }

    /**
     * Constructs an exception wrapping another exception thrown elsewhere.
     */
    public DIRuntimeException(Throwable cause) {
        super(cause);
    }

    public DIRuntimeException(String messageFormat, Throwable cause, Object... messageArgs) {
        // we suppressing stack trace in case this exception has cause, as it is likely irrelevant
        super(String.format(messageFormat, messageArgs), cause, true, cause == null);
    }

    public void setInjectionTrace(InjectionTraceElement[] injectionTrace) {
        this.injectionTrace = injectionTrace;
    }

    public InjectionTraceElement[] getInjectionTrace() {
        return injectionTrace;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(getOriginalMessage());
        if(injectionTrace.length > 0) {
            sb.append("\n\n Injection trace: \n");
            for(int i=0; i<injectionTrace.length; i++) {
                sb.append("\n [").append(i).append(']').append(" resolving key ").append(injectionTrace[i].getBindingKey());
                sb.append("\n    -> ").append(injectionTrace[i].getMessage()).append('\n');
            }
        }
        return sb.toString();
    }

    public String getOriginalMessage() {
        return super.getMessage();
    }
}
