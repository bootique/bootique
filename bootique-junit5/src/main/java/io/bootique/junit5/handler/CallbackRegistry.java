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
package io.bootique.junit5.handler;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

/**
 * @since 2.0
 */
public abstract class CallbackRegistry {

    protected LinkedHashSet<BeforeAllCallback> beforeAll;
    protected LinkedHashSet<BeforeEachCallback> beforeEach;
    protected LinkedHashSet<AfterEachCallback> afterEach;
    protected LinkedHashSet<AfterAllCallback> afterAll;

    public abstract boolean supportedCallback(Field callbackField);

    protected static Object resolveInstance(Object testInstance, Field f) {
        f.setAccessible(true);
        Object instance;
        try {
            instance = f.get(testInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error reading runtime field", e);
        }
        Preconditions.notNull(instance, () -> "Test tool instance '" + f.getName() + "' must be initialized explicitly");
        return instance;
    }

    protected <T> LinkedHashSet<T> addToCallbacks(LinkedHashSet<T> set, T instance) {
        if (set == null) {
            set = new LinkedHashSet<>();
        }

        set.add(instance);
        return set;
    }

    public void beforeAll(ExtensionContext context) throws Exception {
        if (beforeAll != null) {
            for (BeforeAllCallback c : beforeAll) {
                c.beforeAll(context);
            }
        }
    }

    public void beforeEach(ExtensionContext context) throws Exception {
        if (beforeEach != null) {
            for (BeforeEachCallback c : beforeEach) {
                c.beforeEach(context);
            }
        }
    }

    public void afterEach(ExtensionContext context) throws Exception {
        if (afterEach != null) {
            for (AfterEachCallback c : afterEach) {
                c.afterEach(context);
            }
        }
    }


    public void afterAll(ExtensionContext context) throws Exception {
        if (afterAll != null) {
            for (AfterAllCallback c : afterAll) {
                c.afterAll(context);
            }
        }
    }
}
