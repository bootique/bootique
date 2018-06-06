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

package io.bootique;

import io.bootique.command.CommandOutcome;

/**
 * An exception originating in Bootique that indicates an app-wise configuration error, such as invalid CLI parameters,
 * bad YAML format, etc. Usually only its "outcome" property is of any interest (i.e. <i>what</i> happened). Its stack
 * trace (i.e. the place <i>where</i> it happened) is rarely important.
 *
 * @since 0.23
 */
public class BootiqueException extends RuntimeException {

    private CommandOutcome outcome;

    public BootiqueException(int exitCode, String message) {
        this.outcome = CommandOutcome.failed(exitCode, message, this);
    }

    public BootiqueException(int exitCode, String message, Throwable cause) {
        this.outcome = CommandOutcome.failed(exitCode, message, cause);
    }

    public CommandOutcome getOutcome() {
        return outcome;
    }

    @Override
    public Throwable getCause() {
        return outcome.getException() != this ? outcome.getException() : null;
    }

    @Override
    public String getMessage() {
        return outcome.getMessage();
    }
}
