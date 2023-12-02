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

import io.bootique.di.DIRuntimeException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * A class that wraps an annotated method call of an object, passing it DI scope events.
 */
public class ScopeEventBinding {

    private final WeakReference<Object> objectReference;
    private final Method eventHandlerMethod;
    private final int argWidth;

    public ScopeEventBinding(Object object, Method eventHandlerMethod) {

        // store weak references for objects to avoid retaining them when they go out of
        // scope
        this.objectReference = new WeakReference<>(object);
        this.eventHandlerMethod = eventHandlerMethod;
        this.argWidth = eventHandlerMethod.getParameterTypes().length;

        // allow public methods of non-public classes to be annotated
        eventHandlerMethod.setAccessible(true);
    }

    public Object getObject() {
        return objectReference.get();
    }

    public boolean onScopeEvent(Object... eventArgs) {

        Object object = objectReference.get();
        if (object == null) {
            return false;
        }

        try {
            eventHandlerMethod.invoke(object, invocationArguments(eventArgs));
        } catch (Exception e) {
            throw new DIRuntimeException(
                    "Error invoking event method %s",
                    e,
                    eventHandlerMethod.getName());
        }

        return true;
    }

    private Object[] invocationArguments(Object[] eventArgs) {

        int eventArgWidth = (eventArgs == null) ? 0 : eventArgs.length;

        if (argWidth != eventArgWidth) {
            throw new DIRuntimeException(
                    "Event argument list size (%d) is different "
                            + "from the handler method argument list size (%d)",
                    eventArgWidth,
                    argWidth);
        }

        return eventArgs;
    }
}
