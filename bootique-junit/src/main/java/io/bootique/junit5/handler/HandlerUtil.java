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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Field;

/**
 * @since 2.0
 */
public abstract class HandlerUtil {

    public static Object resolveInstance(Object testInstance, Field f) {
        f.setAccessible(true);
        Object instance;
        try {
            instance = f.get(testInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error reading field '" + f.getName() + "'", e);
        } catch (Throwable e) {
            throw new RuntimeException("Error during initialization of field '" + f.getName() + "'", e);
        }

        Preconditions.notNull(instance, () -> "Test instance '" + f.getName()
                + "' is null. It must be initialized explicitly");
        return instance;
    }

    public static ExtensionContext getClassContext(ExtensionContext context) {
        if (context == null) {
            throw new RuntimeException("Can't find org.junit.jupiter.engine.descriptor.ClassExtensionContext in the context hierarchy");
        }

        // access non-public class
        if (context.getClass().getName().equals("org.junit.jupiter.engine.descriptor.ClassExtensionContext")) {
            return context;
        }

        return getClassContext(context.getParent().orElse(null));
    }
}
