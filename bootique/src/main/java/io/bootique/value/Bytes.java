package io.bootique.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.bootique.value.BytesUnit.*;

/**
 * @since 0.26
 */
public class Bytes implements Comparable<Bytes> {

    private static final Pattern TOKENIZER = Pattern.compile("^([0-9]+)\\s*([a-z]+)$");

    private static final Map<String, BytesUnit> UNIT_VOCABULARY;

    static {
        UNIT_VOCABULARY = new HashMap<>();
        UNIT_VOCABULARY.put("b", BYTES);
        UNIT_VOCABULARY.put("byte", BYTES);
        UNIT_VOCABULARY.put("bytes", BYTES);
        UNIT_VOCABULARY.put("kb", KB);
        UNIT_VOCABULARY.put("kilobyte", KB);
        UNIT_VOCABULARY.put("kilobytes", KB);
        UNIT_VOCABULARY.put("mb", MB);
        UNIT_VOCABULARY.put("megabyte", MB);
        UNIT_VOCABULARY.put("megabytes", MB);
        UNIT_VOCABULARY.put("gb", GB);
        UNIT_VOCABULARY.put("gigabyte", GB);
        UNIT_VOCABULARY.put("gigabytes", GB);
    }

    private BytesObject bytes;

    /**
     * Creates a Bytes instance from a String representation. The String has a numeric part, an optional space and
     * a unit part. E.g. "3b", "5 megabytes", "55 gb".
     *
     * @param value a String value representing duration.
     */
    public Bytes(String value) {
        this.bytes = parse(value);
    }


    protected BytesObject parse(String value){

        Objects.requireNonNull(value, "Null 'value' argument");

        if (value.length() == 0) {
            throw new IllegalArgumentException("Empty 'value' argument");
        }

        Matcher matcher = TOKENIZER.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Duration format: " + value);
        }

        BytesUnit unit = parseUnit(matcher.group(2));
        int amount = parseAmount(matcher.group(1));

        return new BytesObject(unit, amount);
    }

    private static int parseAmount(String amount) {
        try {
            return Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time amount: " + amount);
        }
    }

    private static BytesUnit parseUnit(String unitString) {
        BytesUnit unit = UNIT_VOCABULARY.get(unitString);
        if (unit == null) {
            throw new IllegalArgumentException("Invalid time unit: " + unitString);
        }

        return unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BytesObject) {
            return bytes.equals(((BytesObject) obj).getBytes());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return bytes.hashCode();
    }

    public long getBytes() {
        return bytes.getBytes();
    }

    @Override
    public int compareTo(Bytes o) {
        return bytes.compareTo(o.bytes);
    }

    @Override
    public String toString() {
        return bytes.toString();
    }


    private class BytesObject implements Comparable<BytesObject>{

        private long bytes;
        private BytesUnit type;

        public BytesObject(BytesUnit type, long value) {
            this.type = type;
            this.bytes = value * type.getValue();
        }

        public long getBytes() {
            return bytes;
        }

        public BytesUnit getType() {
            return type;
        }

        public long getValue() {
            return bytes / type.getValue();
        }

        @Override
        public int compareTo(BytesObject o) {
            long cmp = Long.compare(bytes, o.getBytes());
            if (cmp != 0) {
                return (int) cmp;
            }
            return (int) (bytes - o.getBytes());
        }


        @Override
        public String toString() {
            return getValue() + " " + type.getName();
        }
    }

}
