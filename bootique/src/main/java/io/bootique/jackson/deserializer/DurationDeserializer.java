package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * Deserializer for Java 8 temporal {@link Duration}s.
 */
class DurationDeserializer extends JSR310DeserializerBase<Duration> {
    private static final long serialVersionUID = 1L;

    public static final DurationDeserializer INSTANCE = new DurationDeserializer();

    private DurationDeserializer() {
        super(Duration.class);
    }

    @Override
    public Duration deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.getCurrentTokenId()) {
            case JsonTokenId.ID_NUMBER_FLOAT:
                BigDecimal value = parser.getDecimalValue();
                long seconds = value.longValue();
                int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
                return Duration.ofSeconds(seconds, nanoseconds);

            case JsonTokenId.ID_NUMBER_INT:
                return Duration.ofSeconds(parser.getLongValue());

            case JsonTokenId.ID_STRING:
                String string = parser.getText().trim();
                if (string.length() == 0) {
                    return null;
                }
                return Duration.parse(string);
        }
        throw context.mappingException("Expected type float, integer, or string.");
    }
}
