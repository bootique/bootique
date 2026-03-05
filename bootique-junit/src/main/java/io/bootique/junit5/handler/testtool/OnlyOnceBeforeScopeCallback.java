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
package io.bootique.junit5.handler.testtool;

import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A callback wrapper that prevents multiple invocations.
 *
 * @since 2.0
 */
public class OnlyOnceBeforeScopeCallback implements BeforeAllCallback {

    private BQBeforeScopeCallback delegate;
    private BQTestScope scope;
    private volatile boolean invoked;

    public OnlyOnceBeforeScopeCallback(BQBeforeScopeCallback delegate, BQTestScope scope) {
        this.scope = scope;
        this.delegate = delegate;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        if (!invoked) {
            synchronized (this) {
                if (!invoked) {
                    try {
                        delegate.beforeScope(scope, context);
                    } finally {
                        invoked = true;
                    }
                }
            }
        }
    }
}
