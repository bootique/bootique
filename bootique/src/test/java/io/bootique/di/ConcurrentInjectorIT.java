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

import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrentInjectorIT {

    @Test
    public void listProvider_NoScope() throws Exception {
        Injector injector = DIBootstrap.createInjector(binder -> {
            binder.bindSet(String.class).addInstance("1").addInstance("2").addInstance("3").withoutScope();
        });

        parallelTest(10000, () ->
                assertEquals(3, injector.getInstance(Key.getSetOf(String.class)).size()));
    }

    @Test
    public void constructorProvider_NoScope() throws Exception {
        Injector injector = DIBootstrap.createInjector(binder -> {
            binder.bindSet(String.class).addInstance("1").addInstance("2").addInstance("3").withoutScope();
            binder.bind(Foo.class).to(FooImpl.class).withoutScope();
        });

        parallelTest(5000, () ->
                assertEquals(4, injector.getInstance(Key.get(Foo.class)).getStrings().size()));
    }

    @Test
    public void constructorProvider_SingletonScope() throws Exception {
        Injector injector = DIBootstrap.createInjector(binder -> {
            binder.bindSet(String.class).addInstance("1").addInstance("2").addInstance("3").inSingletonScope();
            binder.bind(Foo.class).to(FooImplSleep.class).inSingletonScope();
        });

        parallelTest(1000, () ->
                assertEquals(4, injector.getInstance(Key.get(Foo.class)).getStrings().size()));
    }

    @Test
    public void implementationBinding() throws Exception {
        Injector injector = DIBootstrap.createInjector(binder -> {
            binder.bindSet(String.class).addInstance("1").addInstance("2").addInstance("3").inSingletonScope();
            binder.bind(FooImplSleep.class).inSingletonScope();
        });

        parallelTest(1000, () ->
                assertEquals(4, injector.getInstance(Key.get(FooImplSleep.class)).getStrings().size()));
    }

    @Test
    public void dynamicBinding() throws Exception {
        Injector injector = DIBootstrap
                .injectorBuilder(binder -> binder.bindSet(String.class).addInstance("1").addInstance("2").addInstance("3"))
                .defaultSingletonScope()
                .build();

        parallelTest(1000, () ->
                assertEquals(4, injector.getInstance(Key.get(FooImplSleep.class)).getStrings().size()));
    }


    interface Foo {
        Set<String> getStrings();
    }

    static class FooImpl implements Foo {

        Set<String> strings;

        @Inject
        FooImpl(Set<String> strings) {
            strings.add("4");
            this.strings = strings;
        }

        public Set<String> getStrings() {
            return strings;
        }
    }

    static class FooImplSleep implements Foo {

        Set<String> strings;

        @Inject
        FooImplSleep(Set<String> strings) throws InterruptedException {
            strings.add("4");
            this.strings = strings;
            Thread.sleep(100);
        }

        public Set<String> getStrings() {
            return strings;
        }
    }

    private void parallelTest(int iterations, Runnable action) throws Exception {
        parallelTest(4, iterations, action);
    }

    private void parallelTest(int threads, int iterations, Runnable action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable runner = () -> {
            try {
                latch.await();
                for (int i = 0; i < iterations; i++) {
                    action.run();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Latch wait interrupted");
            }
        };

        Future<?>[] futures = new Future[threads];
        for(int i=0; i<threads; i++) {
            futures[i] = executor.submit(runner);
        }

        latch.countDown();

        for(int i=0; i<threads; i++) {
            futures[i].get();
        }
    }

}
