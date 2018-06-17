/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import io.bootique.jackson.deserializer.BQTimeModule;

import java.util.Collection;

public class DefaultJacksonService implements JacksonService {

    private SubtypeResolver subtypeResolver;

    /**
     * @param subtypes a collection of annotated classes to use in subclass resolution.
     * @param <T>      upper boundary of the subclass. Usually {@link io.bootique.config.PolymorphicConfiguration}.
     * @since 0.21
     */
    public <T> DefaultJacksonService(Collection<Class<? extends T>> subtypes) {
        this(toArray(subtypes));
    }

    /**
     * @param subtypes a collection of annotated classes to use in subclass resolution.
     * @since 0.21
     */
    public DefaultJacksonService(Class<?>... subtypes) {
        this.subtypeResolver = new ImmutableSubtypeResolver(subtypes);
    }

    private static <T> Class<?>[] toArray(Collection<Class<? extends T>> subtypes) {
        Class<?>[] array = new Class<?>[subtypes.size()];
        return subtypes.toArray(array);
    }

    @Override
    public ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new BQTimeModule());

        // reusing cached resolver; factory ensures it is immutable...
        mapper.setSubtypeResolver(subtypeResolver);
        return mapper;
    }
}
