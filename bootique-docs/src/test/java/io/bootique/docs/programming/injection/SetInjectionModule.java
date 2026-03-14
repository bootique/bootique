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
package io.bootique.docs.programming.injection;

import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import jakarta.inject.Singleton;

import java.util.Set;

public class SetInjectionModule implements BQModule {

    // tag::set[]
    @Override
    public void configure(Binder binder) {
        binder.bindSet(Servlet.class) // <1>
                .add(Servlet1.class)
                .add(Servlet2.class); // <2>
    }
    // end::set[]

    // tag::provides[]
    @Provides
    @Singleton
    Servlet2 provideServlet2(MyService myService) {
        return new Servlet2(myService);
    }
    // end::provides[]

    // tag::injectSet[]
    @Provides
    @Singleton
    Server provideServer(Set<Servlet> servlets) {
        return new Server(servlets);
    }
    // end::injectSet[]

    // fake servlet
    interface Servlet {
    }

    static class Servlet1 implements Servlet {
    }

    static class Servlet2 implements Servlet {
        private final MyService service;

        public Servlet2(MyService service) {
            this.service = service;
        }
    }

    static class MyService {
    }

    static class Server {
        private final Set<Servlet> servlets;

        public Server(Set<Servlet> servlets) {
            this.servlets = servlets;
        }
    }
}
