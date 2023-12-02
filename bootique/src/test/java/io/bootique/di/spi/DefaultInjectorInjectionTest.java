
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
import io.bootique.di.Key;
import io.bootique.di.TypeLiteral;
import io.bootique.di.mock.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultInjectorInjectionTest {

    @Test
    public void fieldInjection() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(MockInterface2.class).to(MockImplementation2.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertNotNull(service);
        assertEquals("altered_MyName", service.getAlteredName());
    }

    @Test
    public void fieldInjection_Named() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(Key.get(MockInterface1.class, "one")).to(MockImplementation1Alt.class);
            binder.bind(Key.get(MockInterface1.class, "two")).to(MockImplementation1Alt2.class);
            binder.bind(MockInterface2.class).to(MockImplementation2_Named.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertNotNull(service);
        assertEquals("altered_alt", service.getAlteredName());
    }

    @Test
    public void fieldInjectionSuperclass() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(MockInterface2.class).to(MockImplementation2Sub1.class);
            binder.bind(MockInterface3.class).to(MockImplementation3.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertNotNull(service);
        assertEquals("altered_MyName:XName", service.getAlteredName());
    }

    @Test
    public void constructorInjection() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(MockInterface4.class).to(MockImplementation4.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface4 service = injector.getInstance(MockInterface4.class);
        assertNotNull(service);
        assertEquals("constructor_MyName", service.getName());
    }

    @Test
    public void constructorInjection_Named() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(Key.get(MockInterface1.class, "one")).to(MockImplementation1Alt.class);
            binder.bind(Key.get(MockInterface1.class, "two")).to(MockImplementation1Alt2.class);
            binder.bind(MockInterface4.class).to(MockImplementation4Alt.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface4 service = injector.getInstance(MockInterface4.class);
        assertNotNull(service);
        assertEquals("constructor_alt2", service.getName());
    }

    @Test
    public void constructorInjection_Named_Mixed() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(Key.get(MockInterface1.class, "one")).to(MockImplementation1Alt.class);
            binder.bind(Key.get(MockInterface1.class, "two")).to(MockImplementation1Alt2.class);
            binder.bind(MockInterface3.class).to(MockImplementation3.class);
            binder.bind(MockInterface4.class).to(MockImplementation4Alt2.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface4 service = injector.getInstance(MockInterface4.class);
        assertNotNull(service);
        assertEquals("constructor_alt2_XName", service.getName());
    }

    @Test
    public void providerInjection_Constructor() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(MockInterface2.class).to(MockImplementation2_ConstructorProvider.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertEquals("altered_MyName", service.getAlteredName());
    }

    @Test
    public void mapInjection_Empty() {
        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1_MapConfiguration.class);

            // empty map must be still bound
            binder.bindMap(String.class, Object.class, "xyz");
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("", service.getName());
    }

    @Test
    public void mapInjection() {
        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1_MapConfiguration.class);
            binder.bindMap(String.class, Object.class,"xyz")
                    .putInstance("x", "xvalue").putInstance("y", "yvalue").putInstance("x", "xvalue1");
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals(";x=xvalue1;y=yvalue", service.getName());
    }

    @Test
    public void mapWithWildcardInjection() {
        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1_MapWithWildcards.class);
            binder.bindMap(new TypeLiteral<String>(){}, new TypeLiteral<Class<?>>(){})
                    .putInstance("x", String.class).putInstance("y", Integer.class).putInstance("z", Object.class);
        };
        DefaultInjector injector = new DefaultInjector(module);

        // This is example of how to deal with wildcards:
        Map<String, Class<?>> map = injector.getInstance(Key.getMapOf(new TypeLiteral<String>(){}, new TypeLiteral<Class<?>>(){}));

        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals(String.class, map.get("x"));

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("map:3", service.getName());
    }

    @Test
    public void mapInjection_Resumed() {
        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1_MapConfiguration.class);
            // bind 1
            binder.bindMap(String.class, Object.class,"xyz").putInstance("x", "xvalue").putInstance("y", "yvalue");
            // second binding attempt to the same map...
            binder.bindMap(String.class, Object.class,"xyz").putInstance("z", "zvalue").putInstance("x", "xvalue1");
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals(";x=xvalue1;y=yvalue;z=zvalue", service.getName());
    }

    @Test
    public void mapInjection_OverrideExplicitlyBoundType() {
        BQModule m1 = binder -> {
            binder.bind(MockInterface5.class).to(MockImplementation5.class);
            binder.bind(MockInterface1.class).to(MockImplementation1_MapConfiguration.class);

            binder.bindMap(String.class, Object.class, "xyz").put("a", MockInterface5.class);
        };

        BQModule m2 = binder -> binder.bind(MockInterface5.class).toInstance(new MockInterface5() {

            @Override
            public String toString() {
                return "abc";
            }
        });

        MockInterface1 service = new DefaultInjector(m1, m2).getInstance(MockInterface1.class);
        assertEquals(";a=abc", service.getName(), "Map element was not overridden in submodule");
    }

    @Test
    public void mapInjection_OverrideImplicitlyBoundType() {
        BQModule m1 = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1_MapConfiguration.class);
            binder.bindMap(String.class, Object.class, "xyz").put("a", MockImplementation5.class);
        };

        BQModule m2 = binder -> binder.bind(MockImplementation5.class).toInstance(new MockImplementation5() {

            @Override
            public String toString() {
                return "abc";
            }
        });

        MockInterface1 service = new DefaultInjector(m1, m2).getInstance(MockInterface1.class);
        assertEquals(";a=abc", service.getName(), "Map element was not overridden in submodule");
    }


    @Test
    public void injectorInjection() {
        BQModule module = binder -> binder.bind(MockInterface1.class).to(
                MockImplementation1_WithInjector.class);

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("injector_not_null", service.getName());
    }

}
