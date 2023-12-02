
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

package io.bootique.di;

import org.junit.jupiter.api.Test;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KeyTest {

    @Test
    public void baseEquals() {
        Key<String> key1 = Key.get(String.class);
        Key<String> key2 = key1;
        Object key3 = new Object();

        assertEquals(key1, key2);
        assertEquals(key2, key1);
        assertNotEquals(key1, key3);
    }

    @Test
    public void equals() {
        Key<String> key1 = Key.get(String.class);
        Key<String> key2 = Key.get(String.class);
        Key<Integer> key3 = Key.get(Integer.class);
        Key<Integer> key4 = Key.get(Integer.class, "a");
        Key<Integer> key5 = Key.get(Integer.class, "a");
        Key<Integer> key6 = Key.get(Integer.class, "b");
        Key<String> key7 = Key.get(String.class, "a");
        Key<Integer> key8 = Key.get(Integer.class, "");

        assertNull(key1.getBindingName());
        assertTrue(key1.equals(key2));

        assertFalse(key1.equals(key3));

        assertTrue(key3.equals(key8));
        assertNull(key3.getBindingName());
        assertTrue(key8.equals(key3));
        assertNull(key8.getBindingName());

        assertFalse(key3.equals(key4));
        assertFalse(key4.equals(key3));
        assertEquals("a", key4.getBindingName());

        assertTrue(key4.equals(key5));
        assertEquals("a", key5.getBindingName());
        assertTrue(key5.equals(key4));

        assertFalse(key5.equals(key6));
        assertEquals("b", key6.getBindingName());
        assertFalse(key6.equals(key5));

        assertFalse(key4.equals(key7));
        assertEquals("a", key7.getBindingName());
        assertFalse(key7.equals(key4));
    }

    @Test
    public void listKeysEquals() {
        Key<List<Integer>> key1 = Key.getListOf(Integer.class);
        Key<List<String>> key2 = Key.getListOf(String.class);
        Key<List<Integer>> key3 = Key.getListOf(Integer.class);
        Key<List<String>> key4 = Key.getListOf(String.class);

        assertNotEquals(key1, key2);
        assertNotEquals(key3, key4);

        assertEquals(key1, key3);
        assertEquals(key1, key1);
        assertEquals(key2, key4);
        assertEquals(key4, key4);

        // Name is suppressing generic type, to keep backward compatibility.
        Key key5 = Key.getListOf(Object.class, "xyz");
        Key key6 = Key.getListOf(Object.class, "abc");
        assertNotEquals(key5, key6);

        Key key7 = Key.getListOf(Integer.class, "xyz");
        Key key8 = Key.getListOf(Integer.class, "abc");
        assertNotEquals(key7, key8);
        assertNotEquals(key5, key7);

        Key key9 = Key.get(List.class, "xyz");
        assertNotEquals(key7, key9);
    }

    @Test
    public void qualifiedKeysEquals() {
        Key<Integer> key1 = Key.get(Integer.class, CustomQualifier.class);
        Key<Integer> key2 = Key.get(Integer.class);
        Key<Integer> key3 = Key.get(Integer.class, CustomQualifier.class);
        Key<String> key4 = Key.get(String.class, CustomQualifier.class);

        assertEquals(key1, key3);
        assertNotEquals(key1, key2);
        assertNotEquals(key1, key4);

        Key<Map<String, List<? extends Number>>> key5 = Key.get(new TypeLiteral<Map<String, List<? extends Number>>>() {
        }, CustomQualifier.class);
        Key<Map<String, List<? extends Number>>> key6 = Key.get(new TypeLiteral<Map<String, List<? extends Number>>>() {
        }, CustomQualifier.class);
        Key<Map<String, List<? extends Object>>> key7 = Key.get(new TypeLiteral<Map<String, List<? extends Object>>>() {
        }, CustomQualifier.class);

        assertEquals(key5, key6);
        assertNotEquals(key5, key7);
    }

    @Test
    public void hashCodeTest() {
        Key<String> key1 = Key.get(String.class);
        Key<String> key2 = Key.get(String.class);
        Key<Integer> key3 = Key.get(Integer.class);
        Key<Integer> key4 = Key.get(Integer.class, "a");
        Key<Integer> key5 = Key.get(Integer.class, "a");
        Key<Integer> key6 = Key.get(Integer.class, "b");
        Key<String> key7 = Key.get(String.class, "a");

        assertEquals(key1.hashCode(), key1.hashCode(), "generated different hashcode on second invocation");
        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals(key4.hashCode(), key5.hashCode());

        // these are not technically required for hashCode() validity, but as things stand
        // now, these tests will all succeed.
        assertNotEquals(key1.hashCode(), key3.hashCode());
        assertNotEquals(key4.hashCode(), key3.hashCode());
        assertNotEquals(key5.hashCode(), key6.hashCode());
        assertNotEquals(key7.hashCode(), key4.hashCode());
    }

    @Test
    public void qualifiedHashCode() {
        Key<Integer> key1 = Key.get(Integer.class, CustomQualifier.class);
        Key<Integer> key2 = Key.get(Integer.class);
        Key<Integer> key3 = Key.get(Integer.class, CustomQualifier.class);
        Key<String> key4 = Key.get(String.class, CustomQualifier.class);

        assertEquals(key1.hashCode(), key3.hashCode());
        assertNotEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1.hashCode(), key4.hashCode());

        Key<Map<String, List<? extends Number>>> key5 = Key.get(new TypeLiteral<Map<String, List<? extends Number>>>() {
        }, CustomQualifier.class);
        Key<Map<String, List<? extends Number>>> key6 = Key.get(new TypeLiteral<Map<String, List<? extends Number>>>() {
        }, CustomQualifier.class);
        Key<Map<String, List<? extends Object>>> key7 = Key.get(new TypeLiteral<Map<String, List<? extends Object>>>() {
        }, CustomQualifier.class);

        assertEquals(key5.hashCode(), key6.hashCode());
        assertNotEquals(key5.hashCode(), key7.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals("<BindingKey: java.lang.String>",
                Key.get(String.class).toString());
        assertEquals("<BindingKey: java.lang.String, 'xyz'>",
                Key.get(String.class, "xyz").toString());
        assertEquals("<BindingKey: java.util.List[java.lang.String]>",
                Key.getListOf(String.class).toString());
        assertEquals("<BindingKey: java.util.List[java.lang.String], 'xyz'>",
                Key.getListOf(String.class, "xyz").toString());
        assertEquals("<BindingKey: java.util.Map[java.lang.String, java.lang.Integer]>",
                Key.getMapOf(String.class, Integer.class).toString());
    }

    @Test
    public void qualifiedToString() {
        Key<Integer> key1 = Key.get(Integer.class, CustomQualifier.class);
        Key<Map<String, List<? extends Number>>> key2 =
                Key.get(new TypeLiteral<Map<String, List<? extends Number>>>() {
                }, CustomQualifier.class);

        assertEquals("<BindingKey: java.lang.Integer, @io.bootique.di.KeyTest$CustomQualifier>",
                key1.toString());
        assertEquals("<BindingKey: "
                        + "java.util.Map[java.lang.String, java.util.List[io.bootique.di.TypeLiteral$WildcardMarker[java.lang.Object, java.lang.Number]]], "
                        + "@io.bootique.di.KeyTest$CustomQualifier>",
                key2.toString());
    }

    @Qualifier
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomQualifier {
    }
}
