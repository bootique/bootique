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
import io.bootique.config.PolymorphicConfiguration;
import org.junit.Test;

public class PolymorphicConfigurationCheckerIT {

    @Test(expected = AssertionError.class)
    public void test_NotInServiceLoader() {

        // intentionally tricking Java type boundary checks
        Class c1 = C1.class;
        Class c2 = C2.class;
        PolymorphicConfigurationChecker.testNoDefault(c1, c2);
    }

    @Test(expected = AssertionError.class)
    public void testNoDefault_NotInServiceLoader() {

        // intentionally tricking Java type boundary checks
        Class c1 = C1.class;
        Class c2 = C2.class;
        PolymorphicConfigurationChecker.testNoDefault(c1, c2);
    }

    @Test
    public void test_Success() {
        PolymorphicConfigurationChecker.test(C3.class, C4.class, C5.class);
    }

    @Test
    public void test_Success_AbstractSuper() {
        PolymorphicConfigurationChecker.test(C12.class, C13.class);
    }

    @Test
    public void testNoDefault_Success() {
        PolymorphicConfigurationChecker.testNoDefault(C6.class, C7.class, C8.class);
    }

    @Test(expected = AssertionError.class)
    public void testNoDefault_BadDefault() {
        PolymorphicConfigurationChecker.testNoDefault(C9.class, C11.class);
    }

    public static class C1 {
    }

    public static class C2 extends C1 {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = C4.class)
    public static class C3 implements PolymorphicConfiguration {
    }

    @JsonTypeName("c4")
    public static class C4 extends C3 {
    }

    @JsonTypeName("c5")
    public static class C5 extends C3 {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public static class C6 implements PolymorphicConfiguration {
    }

    @JsonTypeName("c7")
    public static class C7 extends C6 {
    }

    @JsonTypeName("c8")
    public static class C8 extends C6 {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = C10.class)
    public static class C9 implements PolymorphicConfiguration {
    }

    @JsonTypeName("c10")
    public static class C10 extends C9 {
    }

    @JsonTypeName("c11")
    public static class C11 extends C9 {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = C13.class)
    public static abstract class C12 implements PolymorphicConfiguration {
    }

    @JsonTypeName("c13")
    public static class C13 extends C12 {
    }

}
