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
package io.bootique.junit5;

import io.bootique.junit5.scope.BQAfterMethodCallback;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BQTestToolCallbackRecorder implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback, BQAfterMethodCallback {

    private List<BQTestToolCallback> callbacks = new ArrayList<>();

    public void assertCallbacksSize(String label, int expectedSize) {

        int actual = 0;
        for (BQTestToolCallback c : callbacks) {
            if (label.equals(c.label)) {
                actual++;
            }
        }

        assertEquals(expectedSize, actual, "Unexpected number of callbacks for: " + label);
    }

    public void assertCallbacksScope(BQTestScope expectedScope) {
        for (BQTestToolCallback c : callbacks) {
            assertEquals(expectedScope, c.scope, "Unexpected scope for " + c);
        }
    }

    public void assertCallback(int pos, String label, BQTestScope scope) {
        assertTrue(pos < callbacks.size(), "No callback at index " + pos +
                ". " + callbacks.size() + " callbacks were registered");
        assertCallback(pos, label, null, scope);
    }

    public void assertCallback(int order, String label, String methodName, BQTestScope scope) {
        callbacks.get(order).assertCallback(label, methodName, scope);
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {
        callbacks.add(new BQTestToolCallback("beforeScope", null, scope));
    }

    @Override
    public void beforeMethod(BQTestScope scope, ExtensionContext context) {
        String method = context.getRequiredTestMethod().getName();
        callbacks.add(new BQTestToolCallback("beforeMethod", method, scope));
    }

    @Override
    public void afterMethod(BQTestScope scope, ExtensionContext context) {
        String method = context.getRequiredTestMethod().getName();
        callbacks.add(new BQTestToolCallback("afterMethod", method, scope));
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        callbacks.add(new BQTestToolCallback("afterScope", null, scope));
    }
}
