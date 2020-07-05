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
package io.bootique.docs.testing;

import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;

@BQTest
public class BQTestToolTest {

    // tag::BQTestTool_static[]
    // implied scope is TEST_CLASS
    @BQTestTool
    final static MyTool tool1 = new MyTool();
    // end::BQTestTool_static[]

    // tag::BQTestTool_instance[]
    // implied scope is TEST_METHOD
    @BQTestTool
    final MyTool tool2 = new MyTool();
    // end::BQTestTool_instance[]

    // tag::BQTestTool_global[]
    // explicit GLOBAL scope. The variable must be static
    @BQTestTool(value = BQTestScope.GLOBAL)
    final static MyTool tool3 = new MyTool();
    // end::BQTestTool_global[]


    static class MyTool {
    }
}
