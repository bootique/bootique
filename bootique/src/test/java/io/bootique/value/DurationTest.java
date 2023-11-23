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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DurationTest {

    @Test
    public void parse_Millis() {
        assertEquals(java.time.Duration.ofMillis(4), Duration.parse("4ms"));
        assertEquals(java.time.Duration.ofSeconds(4), Duration.parse("4000   ms"));
    }

    @Test
    public void parse_Seconds() {
        assertEquals(java.time.Duration.ofSeconds(4), Duration.parse("4sec"));
        assertEquals(java.time.Duration.ofSeconds(4), Duration.parse("4 sec"));
        assertEquals(java.time.Duration.ofSeconds(4), Duration.parse("4s"));
        assertEquals(java.time.Duration.ofSeconds(40), Duration.parse("40 seconds"));
    }

    @Test
    public void parse_Minutes() {
        assertEquals(java.time.Duration.ofMinutes(4), Duration.parse("4min"));
        assertEquals(java.time.Duration.ofMinutes(1), Duration.parse("1 minute"));
        assertEquals(java.time.Duration.ofHours(1), Duration.parse("60 minutes"));
    }

    @Test
    public void parse_Hours() {
        assertEquals(java.time.Duration.ofHours(4), Duration.parse("4hours"));
        assertEquals(java.time.Duration.ofMinutes(60), Duration.parse("1 hr"));
        assertEquals(java.time.Duration.ofHours(1), Duration.parse("1 hour"));
        assertEquals(java.time.Duration.ofHours(5), Duration.parse("5 hrs"));
        assertEquals(java.time.Duration.ofHours(5), Duration.parse("5 h"));
    }

    @Test
    public void parse_Days() {
        assertEquals(java.time.Duration.ofDays(4), Duration.parse("4d"));
        assertEquals(java.time.Duration.ofDays(60), Duration.parse("60 days"));
        assertEquals(java.time.Duration.ofHours(24), Duration.parse("1 day"));
    }

    @Test
    public void parse_Fract() {
        assertEquals(java.time.Duration.ofMillis(1100), Duration.parse("1.1s"));
        assertEquals(java.time.Duration.ofMillis(1235), Duration.parse("1.23456 sec"));
        assertEquals(java.time.Duration.ofMillis(1600), Duration.parse("1.6 sec"));
        assertEquals(java.time.Duration.ofMillis(123), Duration.parse(".123 s"));
        assertEquals(java.time.Duration.ofMillis(543), Duration.parse("0.5433 s"));
        assertEquals(java.time.Duration.ofMillis(126000), Duration.parse("2.1 min"));
        assertEquals(java.time.Duration.ofMillis(12816000), Duration.parse("3.56 h"));
        assertEquals(java.time.Duration.ofMillis(95040000), Duration.parse("1.1days"));
    }

    @Test
    public void parse_Null() {
        assertThrows(NullPointerException.class, () -> Duration.parse(null));
    }

    @Test
    public void parse_FractMs() {
        assertThrows(IllegalArgumentException.class, () -> Duration.parse("1.1 ms"));
    }

    @Test
    public void parse_Empty() {
        assertThrows(IllegalArgumentException.class, () -> Duration.parse(""));
    }

    @Test
    public void parse_Invalid1() {
        assertThrows(IllegalArgumentException.class, () -> Duration.parse("4 nosuchthing"));
    }

    @Test
    public void parse_Invalid2() {
        assertThrows(IllegalArgumentException.class, () -> Duration.parse("not_a_number sec"));
    }

    @Test
    public void compareTo() {
        Duration d1 = new Duration("1s");
        Duration d2 = new Duration("2s");
        Duration d3 = new Duration("1 sec");
        Duration d4 = new Duration("1day");

        Duration d5 = new Duration("1.1s");
        Duration d6 = new Duration("1100ms");

        assertTrue(d1.compareTo(d1) == 0);
        assertTrue(d1.compareTo(d2) < 0);
        assertTrue(d2.compareTo(d1) > 0);
        assertTrue(d1.compareTo(d3) == 0);
        assertTrue(d4.compareTo(d1) > 0);
        assertTrue(d5.compareTo(d6) == 0);
    }

    @Test
    public void equals() {
        Duration d1 = new Duration("1s");
        Duration d2 = new Duration("2s");
        Duration d3 = new Duration("1 sec");
        Duration d4 = new Duration("1000ms");

        Duration d5 = new Duration("1.1s");
        Duration d6 = new Duration("1100ms");

        assertTrue(d1.equals(d1));
        assertFalse(d1.equals(null));
        assertFalse(d1.equals(d2));
        assertTrue(d1.equals(d3));
        assertTrue(d1.equals(d4));
        assertTrue(d4.equals(d1));
        assertTrue(d5.equals(d6));
    }

    @Test
    public void hashCodeTest() {
        Duration d1 = new Duration("1s");
        Duration d2 = new Duration("2s");
        Duration d3 = new Duration("1 sec");
        Duration d4 = new Duration("1000ms");

        assertEquals(d1.hashCode(), d1.hashCode());
        assertEquals(d1.hashCode(), d3.hashCode());
        assertEquals(d1.hashCode(), d4.hashCode());
        assertNotEquals(d1.hashCode(), d2.hashCode());
    }
}
