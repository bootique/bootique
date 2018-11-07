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

package io.bootique.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.bootique.value.BytesUnit.*;

/**
 * Represents a data size value. Used as a value object to deserialize file sizes and such in application configurations.
 *
 * @since 1.0.RC1
 */
public class Bytes implements Comparable<Bytes> {

    private static final Pattern TOKENIZER = Pattern.compile("^([0-9,\\_]+)\\s*([a-zA-Z]+)$");

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
     * @param value a String value representing bytes.
     */
    public Bytes(String value) {
        this.bytes = parse(value);
    }

    private static long parseAmount(String amount) {
        try {
            return Long.parseLong(amount.replaceAll("_", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bytes amount: " + amount);
        }
    }

    private static BytesUnit parseUnit(String unitString) {
        BytesUnit unit = UNIT_VOCABULARY.get(unitString.toLowerCase());
        if (unit == null) {
            throw new IllegalArgumentException("Invalid bytes unit: " + unitString);
        }

        return unit;
    }

    protected BytesObject parse(String value) {

        Objects.requireNonNull(value, "Null 'value' argument");

        if (value.length() == 0) {
            throw new IllegalArgumentException("Empty 'value' argument");
        }

        Matcher matcher = TOKENIZER.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Unit format: " + value);
        }

        BytesUnit unit = parseUnit(matcher.group(2));
        long amount = parseAmount(matcher.group(1));

        return new BytesObject(unit, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bytes) {
            return bytes.equals(((Bytes) obj).getBytes());
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

    /**
     * Returns value in chosen bytes unit
     * @param bytesUnit - target unit
     *
     * @return value in chosen unit
     */
    public long valueOfUnit(BytesUnit bytesUnit) {
        return bytes.getBytes() / bytesUnit.getValue();
    }

    private static class BytesObject implements Comparable<BytesObject> {

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

        @Override
        public int compareTo(BytesObject o) {
            long cmp = Long.compare(bytes, o.getBytes());
            if (cmp != 0) {
                return (int) cmp;
            }
            return (int) (bytes - o.getBytes());
        }

        @Override
        public int hashCode() {
            return Objects.hash(bytes, BYTES.getValue());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Long.class != o.getClass()) return false;
            Long that = (Long) o;
            return bytes == that;
        }

        @Override
        public String toString() {
            return bytes / type.getValue() + " " + type.getName();
        }
    }
}
