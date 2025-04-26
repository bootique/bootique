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

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.bootique.value.BytesUnit.*;

/**
 * Represents a data size value. Used as a value object to deserialize file sizes and such in application configurations.
 */
public class Bytes implements Comparable<Bytes> {

    private static final Pattern TOKENIZER = Pattern.compile("^([0-9]+)\\s*([a-zA-Z]+)$");

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

    private final long bytes;
    private final BytesUnit unit;

    /**
     * Creates a Bytes instance from a String representation. The String has a numeric part, an optional space and
     * a unit part. E.g. "3b", "5 megabytes", "55 gb".
     *
     * @param value a String value representing bytes.
     */
    public Bytes(String value) {
        Objects.requireNonNull(value, "Null 'value' argument");

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty 'value' argument");
        }

        Matcher matcher = TOKENIZER.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Unit format: " + value);
        }

        this.unit = parseUnit(matcher.group(2));
        this.bytes = parseAmount(matcher.group(1)) * unit.getValue();
    }

    private static long parseAmount(String amount) {
        try {
            return Long.parseLong(amount);
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Bytes bytes1)) return false;
        // ignore "unit" in comparison, and just check for the normalized bytes
        return bytes == bytes1.bytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes);
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public int compareTo(Bytes o) {
        long cmp = Long.compare(bytes, o.getBytes());
        if (cmp != 0) {
            return (int) cmp;
        }
        return (int) (bytes - o.getBytes());
    }

    @Override
    @JsonValue
    public String toString() {
        return bytes / unit.getValue() + " " + unit.getName();
    }

    /**
     * Returns value in chosen bytes unit
     *
     * @param bytesUnit - target unit
     * @return value in chosen unit
     */
    public long valueOfUnit(BytesUnit bytesUnit) {
        return bytes / bytesUnit.getValue();
    }
}
