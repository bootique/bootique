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

package io.bootique.meta.application;

import io.bootique.Bootique;

import java.net.URL;
import java.util.Map;

/**
 * A helper class to determine the app main class and user-friendly name.
 */
class ApplicationIntrospector {

    /**
     * Returns application name that is the name of the main class derived from runtime stack.
     *
     * @return application name that is the name of the main class derived from runtime stack.
     */
    static String appNameFromRuntime() {
        Class<?> main = mainClass();
        String name = appNameFromJar(main);
        return name != null ? name : appNameFromClassName(main);
    }

    private static String appNameFromClassName(Class<?> mainClass) {
        // use full class name as app name...
        return mainClass.getName();
    }

    private static String appNameFromJar(Class<?> mainClass) {

        URL url;
        try {
            url = mainClass.getProtectionDomain().getCodeSource().getLocation();
        } catch (Exception e) {
            return null;
        }

        String urlString = url.toExternalForm();

        // e.g. the app is started from an unpacked .jar
        if (!urlString.endsWith(".jar")) {
            return null;
        }

        int slash = urlString.lastIndexOf('/');
        return slash < 0 && slash >= urlString.length() - 1 ? urlString : urlString.substring(slash + 1);
    }


    /**
     * Returns the name of the app main class. If it can't be found, return 'io.bootique.Bootique'.
     *
     * @return the name of the app main class.
     */
    static Class<?> mainClass() {

        for (Map.Entry<Thread, StackTraceElement[]> stackEntry : Thread.getAllStackTraces().entrySet()) {

            // thread must be called "main"
            if ("main".equals(stackEntry.getKey().getName())) {

                StackTraceElement[] stack = stackEntry.getValue();
                StackTraceElement bottom = stack[stack.length - 1];

                // method must be called main
                if ("main".equals(bottom.getMethodName())) {

                    try {
                        return Class.forName(bottom.getClassName());
                    } catch (ClassNotFoundException e) {
                        // can't load class for some reason...
                        return Bootique.class;
                    }

                } else {
                    // no other ideas where else to look for main...
                    return Bootique.class;
                }
            }
        }

        // failover...
        return Bootique.class;
    }
}
