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

import io.bootique.di.Key;

import java.util.LinkedList;
import java.util.List;

/**
 * A helper object that tracks the injection stack to prevent circular dependencies.
 */
class InjectionStack {

    private final ThreadLocal<LinkedList<Key<?>>> stack;

    InjectionStack() {
        this.stack = new ThreadLocal<>();
    }

    void reset() {
        List<Key<?>> localStack = stack.get();
        if (localStack != null) {
            localStack.clear();
        }
    }

    boolean push(Key<?> bindingKey) {
        LinkedList<Key<?>> localStack = stack.get();
        if (localStack == null) {
            localStack = new LinkedList<>();
            stack.set(localStack);
        }

        if (localStack.contains(bindingKey)) {
            return false;
        }

        localStack.add(bindingKey);
        return true;
    }

    void pop() {
        LinkedList<Key<?>> localStack = stack.get();
        if (localStack != null) {
            localStack.removeLast();
        } else {
            throw new IndexOutOfBoundsException("0");
        }
    }

    @Override
    public String toString() {
        List<Key<?>> localStack = stack.get();
        if (localStack != null) {
            return String.valueOf(localStack);
        } else {
            return "[]";
        }
    }
}
