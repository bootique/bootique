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

import static io.bootique.value.BytesUnit.*;
import static org.junit.jupiter.api.Assertions.*;

public class BytesTest {

    @Test
    public void parse_byte() {
        String checkString = 5 + " " + BYTES.getName();
        assertEquals(checkString, new Bytes("5b").toString());
        assertEquals(checkString, new Bytes("5 b").toString());
        assertEquals(checkString, new Bytes("5byte").toString());
        assertEquals(checkString, new Bytes("5 byte").toString());
        assertEquals(checkString, new Bytes("5bytes").toString());
        assertEquals(checkString, new Bytes("5 bytes").toString());
    }

    @Test
    public void parse_KB() {
        assertEquals(5 + " " + KB.getName(),  new Bytes("5kb").toString());
        assertEquals(5120, new Bytes("5kb").getBytes());
        assertEquals(5120, new Bytes("5KB").getBytes());
        assertEquals(5120, new Bytes("5 kb").getBytes());
        assertEquals(5120, new Bytes("5kilobyte").getBytes());
        assertEquals(5120, new Bytes("5 kilobyte").getBytes());
        assertEquals(5120, new Bytes("5kilobytes").getBytes());
        assertEquals(5120, new Bytes("5 kilobytes").getBytes());
    }

    @Test
    public void parse_MB() {
        assertEquals(5 + " " + MB.getName(),  new Bytes("5mb").toString());
        assertEquals(5242880, new Bytes("5mb").getBytes());
        assertEquals(5242880, new Bytes("5MB").getBytes());
        assertEquals(5242880, new Bytes("5 mb").getBytes());
        assertEquals(5242880, new Bytes("5megabyte").getBytes());
        assertEquals(5242880, new Bytes("5 megabyte").getBytes());
        assertEquals(5242880, new Bytes("5megabytes").getBytes());
        assertEquals(5242880, new Bytes("5 megabytes").getBytes());
    }

    @Test
    public void parse_GB() {
        assertEquals(5 + " " + GB.getName(),  new Bytes("5gb").toString());
        assertEquals(5368709120L, new Bytes("5gb").getBytes());
        assertEquals(5368709120L, new Bytes("5GB").getBytes());
        assertEquals(5368709120L, new Bytes("5 gb").getBytes());
        assertEquals(5368709120L, new Bytes("5gigabyte").getBytes());
        assertEquals(5368709120L, new Bytes("5 gigabyte").getBytes());
        assertEquals(5368709120L, new Bytes("5gigabytes").getBytes());
        assertEquals(5368709120L, new Bytes("5 gigabytes").getBytes());
    }

    @Test
    public void parse_Null() {
        assertThrows(NullPointerException.class, () -> new Bytes(null));
    }

    @Test
    public void parse_Empty() {
        assertThrows(IllegalArgumentException.class, () -> new Bytes(""));
    }

    @Test
    public void parse_Invalid1() {
        assertThrows(IllegalArgumentException.class, () -> new Bytes("4 nosuchthing"));
    }

    @Test
    public void parse_Invalid2() {
        assertThrows(IllegalArgumentException.class, () -> new Bytes("not_a_number sec"));
    }

    @Test
    public void compareTo() {
        Bytes b1 = new Bytes("1b");
        Bytes b2 = new Bytes("2b");
        Bytes b3 = new Bytes("2 bytes");
        Bytes b4 = new Bytes("2kb");
        Bytes b5 = new Bytes("2mb");
        Bytes b6 = new Bytes("2gb");

        assertTrue(b1.compareTo(b1) == 0);
        assertTrue(b1.compareTo(b2) < 0);
        assertTrue(b2.compareTo(b1) > 0);
        assertTrue(b2.compareTo(b3) == 0);
        assertTrue(b4.compareTo(b2) > 0);
        assertTrue(b5.compareTo(b4) > 0);
        assertTrue(b6.compareTo(b5) > 0);

    }

    @Test
    public void equals() {
        Bytes b1 = new Bytes("5kb");
        Bytes b2 = new Bytes("5368709120b");
        Bytes b3 = new Bytes("5gb");
        Bytes b4 = new Bytes("5120 b");

        assertTrue(b1.equals(b4));
        assertFalse(b2.equals(null));
        assertTrue(b2.equals(b3));
        assertFalse(b1.equals(b2));
    }

    @Test
    public void hashCodeTest() {
        Bytes b1 = new Bytes("5kb");
        Bytes b2 = new Bytes("5368709120b");
        Bytes b3 = new Bytes("5gb");
        Bytes b4 = new Bytes("5120 b");

        assertEquals(b1.hashCode(), b4.hashCode());
        assertEquals(b2.hashCode(), b3.hashCode());
        assertNotEquals(b1.hashCode(), b3.hashCode());
    }

    @Test
    public void unitConversion() {
        assertEquals(5120, new Bytes("5kb").valueOfUnit(BYTES));
        assertEquals(5, new Bytes("5kb").valueOfUnit(KB));
        assertEquals(5, new Bytes("5mb").valueOfUnit(MB));
        assertEquals(5, new Bytes("5120mb").valueOfUnit(GB));
    }
}
