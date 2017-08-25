package io.bootique.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
        mapper.enable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        // reusing cached resolver; factory ensures it is immutable...
        mapper.setSubtypeResolver(subtypeResolver);
        return mapper;
    }
}
