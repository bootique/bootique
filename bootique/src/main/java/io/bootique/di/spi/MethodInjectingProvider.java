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
import io.bootique.di.TypeLiteral;
import jakarta.inject.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Injection provider that performs injection into object methods annotate with {@link jakarta.inject.Inject}
 * This provider correctly resolves and injects object supertypes' methods.
 *
 * @param <T> type of object for which we perform injection
 */
class MethodInjectingProvider<T> extends MemberInjectingProvider<T> {

    private static final int PRIVATE = 0;
    private static final int PACKAGE = 1;
    private static final int PROTECTED = 2;
    private static final int PUBLIC = 3;

    MethodInjectingProvider(Provider<T> delegate, DefaultInjector injector) {
        super(delegate, injector);
    }

    @Override
    protected void injectMembers(T object, Class<?> type) {
        Map<String, List<Method>> methods = collectMethods(type, new LinkedHashMap<>());
        for (List<Method> methodList : methods.values()){
            methodList.forEach(method -> {
                if (injector.getPredicates().hasInjectAnnotation(method)) {
                    injectMember(object, method);
                }
            });
        }
    }

    /**
     * Collect methods for provided types, including all methods for supertypes
     * wbut without overridden methods.
     *
     * @param type to look up methods
     * @param seenMethods map of already processed methods
     * @return map with all processed methods
     */
    static Map<String, List<Method>> collectMethods(Class<?> type, Map<String, List<Method>> seenMethods) {
        // bail on recursion stop condition
        if (type == Object.class) {
            return seenMethods;
        }

        collectMethods(type.getSuperclass(), seenMethods);

        for (Method method : type.getDeclaredMethods()) {
            // skip static and abstract methods
            if(Modifier.isStatic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            // calculate method signature
            String methodSignature = getMethodSignature(method);

            // lookup for overridden method
            List<Method> parentMethods = seenMethods.computeIfAbsent(methodSignature, sig -> new ArrayList<>());
            parentMethods.removeIf(parentMethod -> isMethodOverride(parentMethod, method));
            parentMethods.add(method);
        }

        return seenMethods;
    }

    /**
     * Check if method overrides parent (assuming basic check performed by compiler)
     */
    private static boolean isMethodOverride(Method parentMethod, Method method) {
        int parentModifier = modifiersToInt(parentMethod.getModifiers());
        if(parentModifier == PRIVATE) {
            // no private methods override
            return false;
        }

        // check class package for package private methods
        if(parentModifier == PACKAGE) {
            return method.getDeclaringClass().getPackage().equals(parentMethod.getDeclaringClass().getPackage());
        }

        // method has same or broader scope, otherwise java compiler should complain
        return true;
    }

    /**
     * Convert access modifiers to int
     *
     * @param methodModifiers all method modifiers
     * @return 0 - private, 1 - package private, 2 - protected, 3 - public
     */
    private static int modifiersToInt(int methodModifiers) {
        if(Modifier.isPrivate(methodModifiers) || Modifier.isStatic(methodModifiers)) {
            return PRIVATE;
        }
        if(Modifier.isProtected(methodModifiers)) {
            return PROTECTED;
        }
        if(Modifier.isPublic(methodModifiers)) {
            return PUBLIC;
        }

        // package private
        return PACKAGE;
    }

    /**
     * Format method signature in arbitrary form that will return same string for same method name,
     * return type and arguments types.
     * NOTE: result is not in Java convention like "public method()Ljava/lang/Object;"
     *
     * @param method to generate signature for
     * @return method signature
     */
    static String getMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append(' ').append(method.getName()).append('(');
        for (Type type : method.getGenericParameterTypes()) {
            sb.append(type.getTypeName()).append(',');
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Do the injection into method
     *
     * @param object to perform inject at
     * @param method to inject
     */
    private void injectMember(Object object, Method method) {

        Object[] values = arguments(method);

        injector.trace(() -> "Injecting method '" + method.getName() + "()' of class " + method.getDeclaringClass().getName());
        method.setAccessible(true);
        try {
            method.invoke(object, values);
        } catch (Exception e) {
            injector.throwException("Error injecting into method '%s()' of class '%s'"
                    , e, method.getName(), method.getDeclaringClass().getName());
        }
    }

    /**
     * @param method to collect arguments for
     * @return values of arguments
     */
    private Object[] arguments(Method method) {

        Type[] parameterTypes = method.getGenericParameterTypes();
        Class<?>[] parameterClasses = method.getParameterTypes();
        Object[] result = new Object[parameterTypes.length];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        InjectionStack stack = injector.getInjectionStack();

        for (int i = 0; i < parameterTypes.length; i++) {
            Type parameterType = parameterTypes[i];
            Annotation bindingAnnotation = getQualifier(parameterAnnotations[i], method);

            int idx = i;
            injector.trace(() -> "Get argument " + idx + " for method '" + method.getName() + "()'" +
                    " of class '" + method.getDeclaringClass().getName() + "'");

            if (injector.getPredicates().isProviderType(parameterClasses[i])) {
                parameterType = GenericTypesUtils.getGenericParameterType(parameterType);
                if (parameterType == null) {
                    injector.throwException("Parameter of method '%s.%s()' of 'Provider' type must be "
                            + "parameterized to be usable for injection"
                            , method.getDeclaringClass().getName()
                            , method.getName());
                }

                result[i] = injector.getJavaxInjectCompatibleProvider(Key.get(TypeLiteral.of(parameterType), bindingAnnotation), parameterClasses[i]);
            } else {
                Key<?> key = Key.get(TypeLiteral.of(parameterType), bindingAnnotation);
                result[i] = injector.getInstanceWithCycleProtection(key);
            }
        }

        return result;
    }

    @Override
    public String getName() {
        return "method injecting provider";
    }
}
