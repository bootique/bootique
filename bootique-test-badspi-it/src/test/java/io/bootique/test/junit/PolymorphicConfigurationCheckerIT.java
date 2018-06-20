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

package io.bootique.test.junit;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.junit.Test;

public class PolymorphicConfigurationCheckerIT {

    @Test(expected = AssertionError.class)
    public void test_NotPolymorphicConfiguration() {

        // intentionally tricking Java type boundary checks
        Class c1 = C1.class;
        Class c2 = C2.class;
        PolymorphicConfigurationChecker.test(c1, c2);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = C2.class)
    public static class C1 {
    }

    @JsonTypeName("c2")
    public static class C2 extends C1 {
    }
}
