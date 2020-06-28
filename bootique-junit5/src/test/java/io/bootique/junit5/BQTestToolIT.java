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

import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQAfterMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(BQTestToolIT.ShutdownTester.class)
@BQTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BQTestToolIT {

    @BQTestTool
    static final Ext1 implicitClassScope = new Ext1();

    @BQTestTool
    final Ext1 implicitMethodScope = new Ext1();

    // TODO: we are not properly testing GLOBAL scope in a single test case...
    @BQTestTool(BQTestScope.GLOBAL)
    static final Ext1 explicitGlobalScope = new Ext1();

    @Test
    @Order(1)
    public void test1() {

        explicitGlobalScope.assertCallback(0, "beforeScope", BQTestScope.GLOBAL);
        explicitGlobalScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.GLOBAL);

        implicitClassScope.assertCallback(0, "beforeScope", BQTestScope.TEST_CLASS);
        implicitClassScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.TEST_CLASS);

        implicitMethodScope.assertCallback(0, "beforeScope", BQTestScope.TEST_METHOD);
        implicitMethodScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.TEST_METHOD);
    }

    @Test
    @Order(2)
    public void test2() {

        explicitGlobalScope.assertCallback(0, "beforeScope", BQTestScope.GLOBAL);
        explicitGlobalScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.GLOBAL);
        explicitGlobalScope.assertCallback(2, "afterMethod", "test1", BQTestScope.GLOBAL);
        explicitGlobalScope.assertCallback(3, "beforeMethod", "test2", BQTestScope.GLOBAL);

        implicitClassScope.assertCallback(0, "beforeScope", BQTestScope.TEST_CLASS);
        implicitClassScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.TEST_CLASS);
        implicitClassScope.assertCallback(2, "afterMethod", "test1", BQTestScope.TEST_CLASS);
        implicitClassScope.assertCallback(3, "beforeMethod", "test2", BQTestScope.TEST_CLASS);

        implicitMethodScope.assertCallback(0, "beforeScope", BQTestScope.TEST_METHOD);
        implicitMethodScope.assertCallback(1, "beforeMethod", "test2", BQTestScope.TEST_METHOD);
    }

    public static class Ext1 implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback, BQAfterMethodCallback {

        private List<Callback> callbacks = new ArrayList<>();

        public void assertCallback(int order, String label, BQTestScope scope) {
            assertTrue(order < callbacks.size(), "No callback at index " + order +
                    ". Only " + callbacks.size() + " callbacks were registered");
            assertCallback(order, label, null, scope);
        }

        public void assertCallback(int order, String label, String methodName, BQTestScope scope) {
            callbacks.get(order).assertCallback(label, methodName, scope);
        }

        @Override
        public void beforeScope(BQTestScope scope, ExtensionContext context) {
            callbacks.add(new Callback("beforeScope", null, scope));
        }

        @Override
        public void beforeMethod(BQTestScope scope, ExtensionContext context) {
            String method = context.getRequiredTestMethod().getName();
            callbacks.add(new Callback("beforeMethod", method, scope));
        }

        @Override
        public void afterMethod(BQTestScope scope, ExtensionContext context) {
            String method = context.getRequiredTestMethod().getName();
            callbacks.add(new Callback("afterMethod", method, scope));
        }

        @Override
        public void afterScope(BQTestScope scope, ExtensionContext context) {
            callbacks.add(new Callback("afterScope", null, scope));
        }
    }

    static class ShutdownTester implements AfterAllCallback {

        @Override
        public void afterAll(ExtensionContext context) {
            explicitGlobalScope.assertCallback(0, "beforeScope", BQTestScope.GLOBAL);
            explicitGlobalScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.GLOBAL);
            explicitGlobalScope.assertCallback(2, "afterMethod", "test1", BQTestScope.GLOBAL);
            explicitGlobalScope.assertCallback(3, "beforeMethod", "test2", BQTestScope.GLOBAL);
            explicitGlobalScope.assertCallback(4, "afterMethod", "test2", BQTestScope.GLOBAL);
            // no "afterScope" for the global scope

            implicitClassScope.assertCallback(0, "beforeScope", BQTestScope.TEST_CLASS);
            implicitClassScope.assertCallback(1, "beforeMethod", "test1", BQTestScope.TEST_CLASS);
            implicitClassScope.assertCallback(2, "afterMethod", "test1", BQTestScope.TEST_CLASS);
            implicitClassScope.assertCallback(3, "beforeMethod", "test2", BQTestScope.TEST_CLASS);
            implicitClassScope.assertCallback(4, "afterMethod", "test2", BQTestScope.TEST_CLASS);
            implicitClassScope.assertCallback(5, "afterScope", BQTestScope.TEST_CLASS);
        }
    }

    static class Callback {

        String label;
        String methodName;
        BQTestScope scope;

        public Callback(String label, String methodName, BQTestScope scope) {
            this.label = label;
            this.methodName = methodName;
            this.scope = scope;
        }

        public void assertCallback(String label, String methodName, BQTestScope scope) {
            assertEquals(label, this.label, "Unexpected callback was invoked: " + this);
            assertEquals(methodName, this.methodName, "Unexpected test method: " + this);
            assertEquals(scope, this.scope, "Unexpected callback scope: " + this);
        }

        @Override
        public String toString() {
            return "{" +
                    "label='" + label + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", scope=" + scope +
                    '}';
        }
    }
}
