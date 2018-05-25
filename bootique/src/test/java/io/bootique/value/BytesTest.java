package io.bootique.value;

import org.junit.Test;

import static io.bootique.value.BytesUnit.*;
import static org.junit.Assert.*;

public class BytesTest {

    @Test
    public void testParse_byte() {
        String checkString = 5 + " " + BYTES.getName();
        assertEquals(checkString, new Bytes("5b").toString());
        assertEquals(checkString, new Bytes("5 b").toString());
        assertEquals(checkString, new Bytes("5byte").toString());
        assertEquals(checkString, new Bytes("5 byte").toString());
        assertEquals(checkString, new Bytes("5bytes").toString());
        assertEquals(checkString, new Bytes("5 bytes").toString());
    }

    @Test
    public void testParse_KB() {
        assertEquals(5 + " " + KB.getName(),  new Bytes("5kb").toString());
        assertEquals(5120, new Bytes("5kb").getBytes());
        assertEquals(5120, new Bytes("5 kb").getBytes());
        assertEquals(5120, new Bytes("5kilobyte").getBytes());
        assertEquals(5120, new Bytes("5 kilobyte").getBytes());
        assertEquals(5120, new Bytes("5kilobytes").getBytes());
        assertEquals(5120, new Bytes("5 kilobytes").getBytes());
    }

    @Test
    public void testParse_MB() {
        assertEquals(5 + " " + MB.getName(),  new Bytes("5mb").toString());
        assertEquals(5242880, new Bytes("5mb").getBytes());
        assertEquals(5242880, new Bytes("5 mb").getBytes());
        assertEquals(5242880, new Bytes("5megabyte").getBytes());
        assertEquals(5242880, new Bytes("5 megabyte").getBytes());
        assertEquals(5242880, new Bytes("5megabytes").getBytes());
        assertEquals(5242880, new Bytes("5 megabytes").getBytes());
    }

    @Test
    public void testParse_GB() {
        assertEquals(5 + " " + GB.getName(),  new Bytes("5gb").toString());
        assertEquals(5368709120L, new Bytes("5gb").getBytes());
        assertEquals(5368709120L, new Bytes("5 gb").getBytes());
        assertEquals(5368709120L, new Bytes("5gigabyte").getBytes());
        assertEquals(5368709120L, new Bytes("5 gigabyte").getBytes());
        assertEquals(5368709120L, new Bytes("5gigabytes").getBytes());
        assertEquals(5368709120L, new Bytes("5 gigabytes").getBytes());
    }

    @Test(expected = NullPointerException.class)
    public void testParse_Null() {
        new Bytes(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_Empty() {
        Duration.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_Invalid1() {
        Duration.parse("4 nosuchthing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_Invalid2() {
        Duration.parse("not_a_number mb");
    }

    @Test
    public void testCompareTo() {
        Duration d1 = new Duration("1s");
        Duration d2 = new Duration("2s");
        Duration d3 = new Duration("1 sec");
        Duration d4 = new Duration("1day");
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
    public void testEquals() {
        Duration d1 = new Duration("1s");
        Duration d2 = new Duration("2s");
        Duration d3 = new Duration("1 sec");
        Duration d4 = new Duration("1000ms");

        assertTrue(d1.equals(d1));
        assertFalse(d1.equals(null));
        assertFalse(d1.equals(d2));
        assertTrue(d1.equals(d3));
        assertTrue(d1.equals(d4));
        assertTrue(d4.equals(d1));
    }

    @Test
    public void testHashCode() {
        Duration d1 = new Duration("1s");
        Duration d2 = new Duration("2s");
        Duration d3 = new Duration("1 sec");
        Duration d4 = new Duration("1000ms");

        assertEquals(d1.hashCode(), d1.hashCode());
        assertEquals(d1.hashCode(), d3.hashCode());
        assertEquals(d1.hashCode(), d4.hashCode());
        assertNotEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    public void testUnitConversion() {
        assertEquals(5, BytesUnit.valueOfUnit(5368709120L, GB));
        assertEquals(5, BytesUnit.valueOfUnit(5242880, MB));
        assertEquals(5, BytesUnit.valueOfUnit(5120, KB));
    }

}
