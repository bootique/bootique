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

import io.bootique.di.InjectionTraceElement;
import io.bootique.di.Key;

import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Optional detailed trace of injection.
 * Can be used in dev environment to create more user-friendly messages in case of injection errors.
 */
class InjectionTrace {

    private ThreadLocal<LinkedList<InjectionTraceElement>> stack;

    InjectionTrace() {
        this.stack = new ThreadLocal<>();
    }

    void push(Key<?> key) {
        getStack().push(new InjectionTraceElement(key));
    }

    void updateMessage(Supplier<String> messageSupplier) {
        InjectionTraceElement element = getStack().peekFirst();
        if(element != null) {
            element.setMessage(messageSupplier);
        }
    }

    InjectionTraceElement pop() {
        return getStack().pollFirst();
    }

    int size() {
        return getStack().size();
    }

    private LinkedList<InjectionTraceElement> getStack() {
        LinkedList<InjectionTraceElement> localStack = stack.get();
        if(localStack == null) {
            localStack = new LinkedList<>();
            stack.set(localStack);
        }
        return localStack;
    }

}
