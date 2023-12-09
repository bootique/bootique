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

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.UnaryOperator;

/**
 * A Jackson "instantiator" of a given Java type that creates objects via a Jackson delegate, but performs injection
 * with Bootique DI providers.
 *
 * @since 3.0
 */
class DIJacksonDelegateInstantiator extends ValueInstantiator {

    private final ValueInstantiator delegate;
    private final UnaryOperator<Object> postInjector;

    public DIJacksonDelegateInstantiator(ValueInstantiator delegate, UnaryOperator<Object> postInjector) {
        this.delegate = delegate;
        this.postInjector = postInjector;
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return delegate.canCreateFromObjectWith();
    }

    @Override
    public boolean canCreateUsingDefault() {
        return delegate.canCreateFromObjectWith();
    }

    @Override
    public boolean canCreateUsingDelegate() {
        return delegate.canCreateUsingDelegate();
    }

    @Override
    public boolean canCreateUsingArrayDelegate() {
        return delegate.canCreateUsingArrayDelegate();
    }

    @Override
    public boolean canCreateFromString() {
        return delegate.canCreateFromString();
    }

    @Override
    public boolean canCreateFromBigDecimal() {
        return delegate.canCreateFromBigDecimal();
    }

    @Override
    public boolean canCreateFromBigInteger() {
        return delegate.canCreateFromBigInteger();
    }

    @Override
    public boolean canCreateFromBoolean() {
        return delegate.canCreateFromBoolean();
    }

    @Override
    public boolean canCreateFromDouble() {
        return delegate.canCreateFromDouble();
    }

    @Override
    public boolean canCreateFromInt() {
        return delegate.canCreateFromInt();
    }

    @Override
    public boolean canCreateFromLong() {
        return delegate.canCreateFromLong();
    }

    @Override
    public boolean canInstantiate() {
        return delegate.canInstantiate();
    }

    @Override
    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
        return delegate.getFromObjectArguments(config);
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
        Object o = delegate.createFromObjectWith(ctxt, args);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, SettableBeanProperty[] props, PropertyValueBuffer buffer) throws IOException {
        Object o = delegate.createFromObjectWith(ctxt, props, buffer);
        return postInjector.apply(o);
    }

    @Override
    public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
        Object o = delegate.createUsingDefault(ctxt);
        return postInjector.apply(o);
    }

    @Override
    public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
        Object o = this.delegate.createUsingDelegate(ctxt, delegate);
        return postInjector.apply(o);
    }

    @Override
    public Object createUsingArrayDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
        Object o = this.delegate.createUsingArrayDelegate(ctxt, delegate);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
        Object o = this.delegate.createFromString(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromBigDecimal(DeserializationContext ctxt, BigDecimal value) throws IOException {
        Object o = this.delegate.createFromBigDecimal(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromBigInteger(DeserializationContext ctxt, BigInteger value) throws IOException {
        Object o = this.delegate.createFromBigInteger(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromBoolean(DeserializationContext ctxt, boolean value) throws IOException {
        Object o = this.delegate.createFromBoolean(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromDouble(DeserializationContext ctxt, double value) throws IOException {
        Object o = this.delegate.createFromDouble(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromInt(DeserializationContext ctxt, int value) throws IOException {
        Object o = this.delegate.createFromInt(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public Object createFromLong(DeserializationContext ctxt, long value) throws IOException {
        Object o = this.delegate.createFromLong(ctxt, value);
        return postInjector.apply(o);
    }

    @Override
    public ValueInstantiator createContextual(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        // DO NOT delegate this method to the "delegate", as it will result in skipping field injection
        return super.createContextual(ctxt, beanDesc);
    }

    @Override
    public JavaType getDelegateType(DeserializationConfig config) {
        return delegate.getDelegateType(config);
    }

    @Override
    public Class<?> getValueClass() {
        return delegate.getValueClass();
    }

    @Override
    public String getValueTypeDesc() {
        return delegate.getValueTypeDesc();
    }
}
