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

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@ExtendWith(BQTestToolIT.ShutdownTester.class)
@BQTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BQTestToolIT {

    @BQTestTool
    static final BQTestToolCallbackRecorder implicitClassScope = new BQTestToolCallbackRecorder();

    @BQTestTool
    final BQTestToolCallbackRecorder implicitMethodScope = new BQTestToolCallbackRecorder();

    // We are not properly testing GLOBAL scope in a single test case, but there is a separate test case for globals
    @BQTestTool(BQTestScope.GLOBAL)
    static final BQTestToolCallbackRecorder explicitGlobalScope = new BQTestToolCallbackRecorder();

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
}
