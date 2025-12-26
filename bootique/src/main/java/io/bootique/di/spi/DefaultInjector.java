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

import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.DIRuntimeException;
import io.bootique.di.InjectionTraceElement;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.Scope;
import io.bootique.log.BootLogger;
import jakarta.inject.Provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A default implementations of a DI injector.
 */
public class DefaultInjector implements Injector {

    public enum Options {
        SINGLETON_SCOPE_BY_DEFAULT,
        DECLARED_OVERRIDE_ONLY,
        DISABLE_DYNAMIC_BINDINGS,
        ENABLE_METHOD_INJECTION,
        DISABLE_TRACE,
        DISABLE_PROXY
    }

    private final DefaultScope singletonScope;
    private final Scope noScope;
    private final Scope defaultScope;

    private final Map<Key<?>, Binding<?>> bindings;
    private final Map<Key<?>, Decoration<?>> decorations;
    private final InjectionStack injectionStack;
    private final InjectionTrace injectionTrace;
    private final InjectorPredicates predicates;
    private final Set<Key<?>> earlySetupSet;
    private final Map<Class<?>, List<Key<?>>> keysByRawType;

    private final boolean allowDynamicBinding;
    private final boolean allowOverride;
    private final boolean allowMethodInjection;
    private final boolean injectionTraceEnabled;
    private final boolean allowProxyCreation;

    private volatile boolean isShutdown;

    DefaultInjector(BQModule... modules) {
        this(Collections.emptySet(), new InjectorPredicates(), modules);
    }

    public DefaultInjector(Set<Options> options, InjectorPredicates predicates, BQModule... modules) {
        this.predicates = predicates;

        this.singletonScope = new DefaultScope();
        this.noScope = NoScope.INSTANCE;
        this.defaultScope = options.contains(Options.SINGLETON_SCOPE_BY_DEFAULT) ? singletonScope : noScope;

        this.allowOverride = !options.contains(Options.DECLARED_OVERRIDE_ONLY);
        this.allowDynamicBinding = !options.contains(Options.DISABLE_DYNAMIC_BINDINGS);
        this.allowMethodInjection = options.contains(Options.ENABLE_METHOD_INJECTION);
        this.injectionTraceEnabled = !options.contains(Options.DISABLE_TRACE);
        this.allowProxyCreation = !options.contains(Options.DISABLE_PROXY);

        this.bindings = new ConcurrentHashMap<>();
        this.decorations = new ConcurrentHashMap<>();
        this.injectionStack = new InjectionStack();
        this.injectionTrace = injectionTraceEnabled ? new InjectionTrace() : null;
        this.earlySetupSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.keysByRawType = new ConcurrentHashMap<>();

        Binder binder = new DefaultBinder(this);

        // bind self for injector injection...
        binder.bind(Injector.class).toInstance(this);

        // bind modules
        if (modules != null && modules.length > 0) {
            ProvidesHandler providesHandler = new ProvidesHandler(this);

            for (BQModule module : modules) {
                module.configure(binder);
                providesHandler.bindingsFromAnnotatedMethods(module);
            }
        }

        applyDecorators();
        earlySetup();
    }

    InjectionStack getInjectionStack() {
        return injectionStack;
    }

    InjectorPredicates getPredicates() {
        return predicates;
    }

    @SuppressWarnings("unchecked")
    <T> Binding<T> getBinding(Key<T> key) {
        if (isShutdown) {
            throwException("Injector is shutdown");
        }
        // may return null - this is intentionally allowed in this non-public method
        return (Binding<T>) bindings.get(Objects.requireNonNull(key, "Null key"));
    }

    <T> void putBinding(Key<T> bindingKey, Provider<T> provider) {
        putBinding(bindingKey, new Binding<>(bindingKey, wrapProvider(bindingKey, provider), defaultScope, false));
    }

    <T> void putOptionalBinding(Key<T> bindingKey, Provider<T> provider) {
        putBinding(bindingKey, new Binding<>(bindingKey, wrapProvider(bindingKey, provider), defaultScope, true));
    }

    /**
     * Override existing binding, will throw if no binding exists for given key.
     */
    <T> void overrideBinding(Key<T> bindingKey, Provider<T> provider) {
        if (isShutdown) {
            throwException("Injector is shutdown");
        }
        Binding<T> binding = new Binding<>(bindingKey, wrapProvider(bindingKey, provider), defaultScope, false);
        Binding<?> oldBinding = bindings.put(bindingKey, binding);
        if (oldBinding == null) {
            throwException("No binding to override for key %s", bindingKey);
        }
    }

    <T> void putBinding(Key<T> bindingKey, Binding<T> binding) {
        if (isShutdown) {
            throwException("Injector is shutdown");
        }
        Binding<?> oldBinding = bindings.put(bindingKey, binding);
        if (oldBinding == null) {
            keysByRawType.computeIfAbsent(bindingKey.getType().getRawType(), type -> new ArrayList<>(1))
                    .add(bindingKey);
        }
        if (!canOverride(oldBinding)) {
            throwException("Unable to override key %s. It is final and override is disabled.", bindingKey);
        }
    }

    /**
     * <ul>
     *     <li> Can always override optional bindings
     *     <li> Can override if new binding marked as override
     *     <li> Can always override if Injector configured with overrides enabled
     * </ul>
     *
     * @param oldBinding existing binding (or null if absent) to override
     * @return can binding be overridden with new one
     */
    private boolean canOverride(Binding<?> oldBinding) {
        // binding provider can be null if it is incomplete (e.g. binder.bind(MyClass.class);)
        return oldBinding == null
                || oldBinding.getOriginal() == null
                || oldBinding.isOptional()
                || allowOverride;
    }

    @SuppressWarnings("unchecked")
    private <T> Decoration<T> getDecoration(Key<T> bindingKey) {
        if (isShutdown) {
            throwException("Injector is shutdown");
        }
        return (Decoration<T>) decorations.computeIfAbsent(bindingKey, bk -> new Decoration<>());
    }

    <T> void putDecorationAfter(Key<T> bindingKey, DecoratorProvider<T> decoratorProvider) {
        getDecoration(bindingKey).after(decoratorProvider);
    }

    <T> void putDecorationBefore(Key<T> bindingKey, DecoratorProvider<T> decoratorProvider) {
        getDecoration(bindingKey).before(decoratorProvider);
    }

    <T> void changeBindingScope(Key<T> bindingKey, Scope scope) {
        if (scope == null) {
            scope = noScope;
        }

        Binding<?> binding = bindings.get(bindingKey);
        if (binding == null) {
            throwException("No existing binding for key " + bindingKey);
            return;
        }

        binding.changeScope(scope);
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        return getInstanceWithCycleProtection(key, false);
    }

    <T> T getInstanceWithCycleProtection(Key<T> key, boolean fromProxy) {
        if (!injectionStack.push(key)) {
            // cycle detected in dependency
            // 1. try to create proxy
            if (allowProxyCreation && !fromProxy) {
                T proxy = getProxyInstance(key);
                if (proxy != null) {
                    return proxy;
                }
            }
            // 2. throw if failed
            throwException(
                    "Circular dependency detected when binding a key %s. Nested keys: %s"
                            + ". To resolve it, you should inject a Provider instead of an object.",
                    key,
                    injectionStack);
        }

        try {
            return getProvider(key).get();
        } finally {
            injectionStack.pop();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T getProxyInstance(Key<T> key) {
        Class<T> bindingClass = (Class) key.getType().getRawType();
        if (!bindingClass.isInterface()) {
            return null;
        }
        InvocationHandler handler = new ProxyInvocationHandler<>(this, key);
        T proxyInstance = (T) Proxy.newProxyInstance(bindingClass.getClassLoader(), new Class<?>[]{bindingClass}, handler);
        trace(() -> "Create proxy for binding " + key);
        return proxyInstance;
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) throws DIRuntimeException {
        Binding<T> binding = getBinding(key);
        if (binding == null || binding.getOriginal() == null) {
            binding = createDynamicBinding(key);
        }

        return predicates.wrapProvider(binding.getScoped());
    }

    @Override
    public boolean hasProvider(Key<?> key) {
        return getBinding(key) != null;
    }

    @SuppressWarnings("unchecked")
    private <T> Binding<T> createDynamicBinding(Key<T> key) {
        // Compute new bindings for given key
        return (Binding<T>) bindings.compute(key, (k, oldBinding) -> {
            if (oldBinding == null && !allowDynamicBinding) {
                throwException("DI container has no binding for key %s and dynamic bindings are disabled.", key);
            }

            if (oldBinding != null && oldBinding.getOriginal() != null) {
                return oldBinding;
            }

            Class<T> implementation = (Class<T>) key.getType().getRawType();
            Provider<T> provider = new ConstructorInjectingProvider<>(implementation, this);

            Scope scope = defaultScope;
            if (oldBinding != null && oldBinding.getScope() != defaultScope) {
                scope = oldBinding.getScope();
            } else if (getPredicates().isSingleton(implementation)) {
                scope = singletonScope;
            }

            return new Binding<>(key, wrapInMemberInjectionProviders(key, provider), scope, false);
        });
    }

    private <T> Provider<T> wrapInMemberInjectionProviders(Key<T> key, Provider<T> provider) {
        Provider<T> provider1 = new FieldInjectingProvider<>(provider, this);
        if (isMethodInjectionEnabled()) {
            provider1 = new MethodInjectingProvider<>(provider1, this);
        }

        return wrapProvider(key, provider1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void injectMembers(Object object) {
        Key<Object> key = Key.get((Class<Object>) object.getClass());
        wrapInMemberInjectionProviders(key, new InstanceProvider<>(object)).get();
    }

    @Override
    public synchronized void shutdown() {
        if (isShutdown) {
            return;
        }
        isShutdown = true;
        singletonScope.shutdown();
        bindings.clear();
        decorations.clear();
        injectionStack.reset();
        keysByRawType.clear();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> Collection<Key<T>> getKeysByType(Class<T> type) {
        return (List) keysByRawType.getOrDefault(type, Collections.emptyList());
    }

    @Override
    public void reportWarnings(BootLogger logger) {
        // Was used to report warnings for "javax.inject" use. A noop for now, but keeping around we need it in the
        // future
    }

    DefaultScope getSingletonScope() {
        return singletonScope;
    }

    Scope getDefaultScope() {
        return defaultScope;
    }

    Scope getNoScope() {
        return noScope;
    }

    boolean isMethodInjectionEnabled() {
        return allowMethodInjection;
    }

    boolean isInjectionTraceEnabled() {
        return injectionTraceEnabled;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void applyDecorators() {
        for (Entry<Key<?>, Decoration<?>> e : decorations.entrySet()) {

            Binding b = bindings.get(e.getKey());
            if (b == null) {
                // TODO: print warning - decorator of a non-existing service..
                continue;
            }

            b.decorate(this, e.getValue());
        }
    }

    void markForEarlySetup(Key<?> key) {
        changeBindingScope(key, getSingletonScope());
        earlySetupSet.add(key);
    }

    /**
     * Init all services that a marked for early setup
     */
    private void earlySetup() {
        earlySetupSet.forEach(this::getInstance);
        earlySetupSet.clear();
    }

    /**
     * Wraps provider in traceable provider if trace is enabled
     */
    <T> Provider<T> wrapProvider(Key<T> key, Provider<T> provider) {
        if (!injectionTraceEnabled || provider == null) {
            return provider;
        }

        return new TraceableProvider<>(key, provider, this);
    }

    /**
     * @param messageSupplier trace message supplier
     */
    void trace(Supplier<String> messageSupplier) {
        if (injectionTraceEnabled) {
            injectionTrace.updateMessage(messageSupplier);
        }
    }

    /**
     * Push currently resolving key into trace stack
     *
     * @param key that is resolving
     */
    void tracePush(Key<?> key) {
        if (injectionTraceEnabled) {
            injectionTrace.push(key);
        }
    }

    /**
     * Pop key out of trace stack
     */
    void tracePop() {
        if (injectionTraceEnabled) {
            injectionTrace.pop();
        }
    }

    /**
     * Formats and throws DI exception with injection trace attached to it.
     * <p>
     * Can be used anywhere like {@code return throwException("some message");}
     *
     * @param message message
     * @param args    message format arguments
     * @param <T>     generic type to return
     * @return nothing, always throws
     * @throws DIRuntimeException throws DI exception always
     */
    <T> T throwException(String message, Object... args) throws DIRuntimeException {
        throw setTrace(predicates.createException(message, args));
    }

    /**
     * Formats and throws DI exception with injection trace attached to it.
     * <p>
     * Can be used anywhere like {@code return throwException("some message", cause);}
     * <p>
     * This method will try to unwrap {@code cause}
     * <ul>
     *     <li>if it is another {@link DIRuntimeException} it will be rethrown as is
     *     <li>if it is {@link InvocationTargetException} it cause will be used as cause for DI exception
     *     <li>otherwise cause will be used as is
     * </ul>
     *
     * @param message message
     * @param cause   underlying cause of this exception
     * @param args    message format arguments
     * @param <T>     generic type to return
     * @return nothing, always throws
     * @throws DIRuntimeException throws DI exception always
     */
    <T> T throwException(String message, Throwable cause, Object... args) throws DIRuntimeException {
        if (cause instanceof InvocationTargetException && cause.getCause() != null) {
            // if reflection method call thrown an exception unwrap and use it as a cause
            cause = cause.getCause();
        }

        // TODO: is assumption bellow always correct?
        // If it was other DI exception, use it. It will better point to the actual problem.
        if (cause instanceof DIRuntimeException die) {
            throw die;
        }

        throw setTrace(predicates.createException(message, cause, args));
    }


    /**
     * Set trace (if any) to exception
     *
     * @param ex exception
     * @return ex
     */
    private DIRuntimeException setTrace(DIRuntimeException ex) {
        if (!injectionTraceEnabled) {
            return ex;
        }

        InjectionTraceElement[] traceElements = new InjectionTraceElement[injectionTrace.size()];
        InjectionTraceElement element;
        int i = 0;
        while ((element = injectionTrace.pop()) != null) {
            traceElements[i++] = element;
        }

        ex.setInjectionTrace(traceElements);
        return ex;
    }

}
