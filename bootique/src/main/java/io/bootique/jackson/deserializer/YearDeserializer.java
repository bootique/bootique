package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/**
 * Deserializer for Java 8 temporal {@link Year}s.
 */
class YearDeserializer extends JSR310DeserializerBase<Year> {
    private static final long serialVersionUID = 1L;
    private DateTimeFormatter formatter;
    public static final YearDeserializer INSTANCE = new YearDeserializer();

    private YearDeserializer() {
        super(Year.class);
    }

    public YearDeserializer(DateTimeFormatter formatter) {
        super(Year.class);
        this.formatter = formatter;
    }

    @Override
    public Year deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (formatter == null) {
            return Year.of(parser.getValueAsInt());
        }
        return Year.parse(parser.getValueAsString(), formatter);
    }
}
