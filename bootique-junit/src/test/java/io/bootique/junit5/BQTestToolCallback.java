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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BQTestToolCallback {

    String label;
    String methodName;
    BQTestScope scope;

    public BQTestToolCallback(String label, String methodName, BQTestScope scope) {
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
