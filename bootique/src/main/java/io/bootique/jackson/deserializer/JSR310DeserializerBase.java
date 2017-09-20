package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;
import java.time.DateTimeException;

/**
 * Base class that indicates that all JSR310 datatypes are deserialized from scalar JSON types.
 */
abstract class JSR310DeserializerBase<T> extends StdScalarDeserializer<T> {
    private static final long serialVersionUID = 1L;

    protected JSR310DeserializerBase(Class<T> supportedType) {
        super(supportedType);
    }

    @Override
    public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer deserializer)
            throws IOException {
        return deserializer.deserializeTypedFromAny(parser, context);
    }

    protected void _reportWrongToken(JsonParser parser, DeserializationContext context,
                                     JsonToken exp, String unit) throws IOException {
        context.wrongTokenException(parser, JsonToken.VALUE_NUMBER_INT,
                "Expected " + exp.name() + " for '" + unit + "' of " + handledType().getName() + " value");
    }

    /**
     * Helper method used to peel off spurious wrappings of DateTimeException
     */
    protected DateTimeException _peelDTE(DateTimeException e) {
        while (true) {
            Throwable t = e.getCause();
            if (t != null && t instanceof DateTimeException) {
                e = (DateTimeException) t;
                continue;
            }
            break;
        }
        return e;
    }
}
