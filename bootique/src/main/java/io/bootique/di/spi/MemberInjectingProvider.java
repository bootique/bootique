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

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

/**
 * Base abstract implementation for providers injecting into object members (fields and methods)
 *
 * @param <T> type of object for which we perform injection
 */
abstract class MemberInjectingProvider<T> implements NamedProvider<T> {

    protected final DefaultInjector injector;
    protected final Provider<T> delegate;

    MemberInjectingProvider(Provider<T> delegate, DefaultInjector injector) {
        this.delegate = delegate;
        this.injector = injector;
    }

    @Override
    public T get() {
        T result;
        try {
            result = delegate.get();
        } catch (Exception ex) {
            return injector.throwException("Underlying provider (%s) thrown exception", ex, DIUtil.getProviderName(delegate));
        }
        if(result == null) {
            return injector.throwException("Underlying provider (%s) returned NULL instance", DIUtil.getProviderName(delegate));
        }
        injectMembers(result, result.getClass());
        return result;
    }

    abstract void injectMembers(T object, Class<?> aClass);

    Annotation getQualifier(Annotation[] annotations, AccessibleObject object) {
        Annotation bindingAnnotation = null;
        for(Annotation fieldAnnotation : annotations) {
            if(injector.getPredicates().isQualifierAnnotation(fieldAnnotation)) {
                if(bindingAnnotation != null) {
                    injector.throwException("Found more than one qualifier annotation for '%s.%s'."
                            , ((Member)object).getDeclaringClass().getName()
                            , ((Member)object).getName());
                }
                bindingAnnotation = fieldAnnotation;
            }
        }
        return bindingAnnotation;
    }

    Annotation getQualifier(AccessibleObject object) {
        return getQualifier(object.getAnnotations(), object);
    }
}
