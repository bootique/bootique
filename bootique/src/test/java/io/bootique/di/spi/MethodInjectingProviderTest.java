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

package io.bootique.di.spi;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodInjectingProviderTest {

    @Test
    public void collectMethods() {
        Map<String, List<Method>> methods = MethodInjectingProvider
                .collectMethods(Class2.class, new LinkedHashMap<>());

        assertEquals(3, methods.size());

        String[] methodSig = {"int methodInt()", "void methodArgs(int,java.lang.Object,)", "void method1()"};
        for(String sig : methodSig) {
            assertTrue(methods.containsKey(sig), () -> "No method " + sig);
        }

        List<Method> methodList = methods.get(methodSig[0]);
        assertEquals(1, methodList.size());
        assertEquals(Class2.class, methodList.get(0).getDeclaringClass());

        methodList = methods.get(methodSig[1]);
        assertEquals(2, methodList.size());
        assertEquals(Class1.class, methodList.get(0).getDeclaringClass());
        assertEquals(Class2.class, methodList.get(1).getDeclaringClass());

        methodList = methods.get(methodSig[2]);
        assertEquals(1, methodList.size());
        assertEquals(Class2.class, methodList.get(0).getDeclaringClass());
    }

    @Test
    public void getMethodSignature() throws Exception {
        Method methodInt = Class1.class.getDeclaredMethod("methodInt");
        Method methodArgs = Class1.class.getDeclaredMethod("methodArgs", int.class, Object.class);

        String signature1 = MethodInjectingProvider.getMethodSignature(methodInt);
        assertEquals("int methodInt()", signature1);

        String signature2 = MethodInjectingProvider.getMethodSignature(methodArgs);
        assertEquals("void methodArgs(int,java.lang.Object,)", signature2);
    }

    static class Class1 {

        static void method() {
        }

        public void method1() {
        }

        int methodInt() {
            return 0;
        }

        private void methodArgs(int i, Object obj) {
        }
    }

    static class Class2 extends Class1 {

        static void method() {
        }

        @Override
        public void method1() {
        }

        @Override
        int methodInt() {
            return 1;
        }

        private void methodArgs(int i, Object obj) {
        }
    }
}