package io.bootique.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import io.bootique.log.BootLogger;

public class DefaultJacksonService implements JacksonService {

    private BootLogger bootLogger;
    private SubtypeResolver subtypeResolver;

    public DefaultJacksonService(SubtypeResolver subtypeResolver, BootLogger bootLogger) {
        this.bootLogger = bootLogger;
        this.subtypeResolver = subtypeResolver;
    }

    @Override
    public ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // reusing cached resolver; factory ensures it is immutable...
        mapper.setSubtypeResolver(subtypeResolver);
        return mapper;
    }
}
