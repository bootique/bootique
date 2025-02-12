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

package io.bootique.di;

import io.bootique.BQModule;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DIErrorsIT {

    @Test
    public void longInjectionChainFailure() {

        // deep injection error:
        // instantiate foo -> inject field bar
        //      -> bar provider -> inject constructor arg baz
        //      -> baz provider method -> inject arg QuuxConsumer
        //      -> QuuxConsumer -> inject field quux
        //      -> Quux implementation -> inject method arg Map<>
        //      -> Map provider -> resolve qux object
        //      -> qux provider method -> null
        //          -> should throw
        Injector injector = DIBootstrap.injectorBuilder(new TestModule()).enableMethodInjection().build();

        // check injection trace
        try {
            injector.getInstance(Foo.class);
            fail("Should throw DIRuntimeException");
        } catch (DIRuntimeException ex) {
            String message = ex.getOriginalMessage();
            String fullMessage = ex.getMessage();
            assertTrue(message.contains("returned NULL instance"), message);
            assertTrue(fullMessage.contains("returned NULL instance"), fullMessage);
            assertTrue(fullMessage.contains("Invoking provider method 'createQux()' of module 'io.bootique.di.DIErrorsIT$TestModule'"), fullMessage);
            assertTrue(fullMessage.contains("Injecting field 'bar' of class io.bootique.di.DIErrorsIT$FooImpl"), fullMessage);

            InjectionTraceElement[] traceElements = ex.getInjectionTrace();
            assertEquals(9, traceElements.length);
            assertEquals(Key.get(Qux.class), traceElements[0].getBindingKey());
            assertEquals(Key.getMapOf(String.class, Object.class), traceElements[1].getBindingKey());
            assertEquals(Key.get(Foo.class), traceElements[8].getBindingKey());
        }

        // check that trace is clean for second exception
        try {
            injector.getInstance(Qux.class);
            fail("Should throw DIRuntimeException");
        } catch (DIRuntimeException ex) {
            String message = ex.getOriginalMessage();
            String fullMessage = ex.getMessage();
            assertTrue(message.contains("returned NULL instance"), message);
            assertTrue(fullMessage.contains("returned NULL instance"), fullMessage);
            assertTrue(fullMessage.contains("Invoking provider method 'createQux()' of module 'io.bootique.di.DIErrorsIT$TestModule'"), fullMessage);

            InjectionTraceElement[] traceElements = ex.getInjectionTrace();
            assertEquals(1, traceElements.length);
            assertEquals(Key.get(Qux.class), traceElements[0].getBindingKey());
        }
    }

    @Test
    public void decorationFailure() {
        Injector injector = DIBootstrap.createInjector(binder -> {
            binder.bind(Bar.class).to(BarImpl2.class);
            binder.bind(Baz.class).to(BazImpl2.class);
            binder.bind(Foo.class).to(FooImpl5.class);
            binder.decorate(Foo.class).before(DecoratedFoo.class);
        });

        try {
            injector.getInstance(Foo.class);
            fail("Should throw DIRuntimeException");
        } catch (DIRuntimeException ex) {
            InjectionTraceElement[] traceElements = ex.getInjectionTrace();
            assertEquals(5, traceElements.length);
            assertEquals(Key.get(BarImpl2.class),   traceElements[0].getBindingKey());
            assertEquals(Key.get(Bar.class),        traceElements[1].getBindingKey());
            assertEquals(Key.get(BazImpl2.class),   traceElements[2].getBindingKey());
            assertEquals(Key.get(Baz.class),        traceElements[3].getBindingKey());
            assertEquals(Key.get(Foo.class),        traceElements[4].getBindingKey());
        }
    }

    private static class TestModule implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bind(Foo.class).to(FooImpl.class);
            binder.bind(Bar.class).toJakartaProvider(BarProvider.class);
            binder.bindMap(String.class, Object.class)
                    .putInstance("key1", new Object())
                    .put("key2", Key.get(Qux.class));
            binder.bind(Quux.class).to(QuuxImpl.class);
            binder.bind(QuuxConsumer.class);
        }

        @Provides
        Baz createBaz(QuuxConsumer consumer) {
            return new Baz() {};
        }

        @Provides
        static Qux createQux() {
            return null;
        }
    }

    private static class QuuxImpl implements Quux {
        @Inject
        void setMap(Map<String, Object> map) {
        }
    }

    private static class FooImpl implements Foo {
        @Inject
        private Bar bar;
    }

    private static class QuuxConsumer {
        @Inject
        Quux quux;
    }

    private static class BarProvider implements Provider<Bar> {
        @Inject
        BarProvider(Baz baz) {
        }

        @Override
        public Bar get() {
            return new Bar() {};
        }
    }

    private static class FooImpl2 implements Foo {}
    private static class FooImpl3 implements Foo {}
    private static class FooImpl4 implements Foo {}

    private static class FooImpl5 implements Foo {
        @Inject
        Baz baz;
    }

    private static class DecoratedFoo implements Foo {

        Provider<Foo> foo;

        @Inject
        Baz baz;

        @Inject
        DecoratedFoo(Provider<Foo> foo) {
            this.foo = foo;
        }

        public String doIt() {
            return "decorated " + foo.get().doIt();
        }
    }

    private static class BarImpl2 implements Bar {
        @Inject
        List<Foo> fooList;
    }

    private static class BazImpl2 implements Baz {
        @Inject
        Bar bar;
    }

    interface Foo  {
        default String doIt() {
            return "foo";
        };
    }
    interface Bar  {}
    interface Baz  {}
    interface Qux  {}
    interface Quux {}

    interface ExtendedFoo extends Foo {
    }

    static class FooImplementation implements ExtendedFoo {
        @Override
        public String doIt() {
            return "FooImplementation";
        }
    }
}
