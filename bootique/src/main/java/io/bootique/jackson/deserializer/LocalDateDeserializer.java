package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Deserializer for Java 8 temporal {@link LocalDate}s.
 */
class LocalDateDeserializer extends JSR310DateTimeDeserializerBase<LocalDate> {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static final LocalDateDeserializer INSTANCE = new LocalDateDeserializer();

    private LocalDateDeserializer() {
        this(DEFAULT_FORMATTER);
    }

    public LocalDateDeserializer(DateTimeFormatter dtf) {
        super(LocalDate.class, dtf);
    }

    @Override
    protected JsonDeserializer<LocalDate> withDateFormat(DateTimeFormatter dtf) {
        return new LocalDateDeserializer(dtf);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String string = parser.getText().trim();
            if (string.length() == 0) {
                return null;
            }
            // as per [datatype-jsr310#37], only check for optional (and, incorrect...) time marker 'T'
            // if we are using default formatter
            DateTimeFormatter format = _formatter;
            if (format == DEFAULT_FORMATTER) {
                if (string.contains("T")) {
                    return LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            }
            return LocalDate.parse(string, format);
        }
        throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
    }
}
