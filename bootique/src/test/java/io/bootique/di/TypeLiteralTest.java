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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class TypeLiteralTest {

    @Test
    public void baseEqualsInvariants() {
        TypeLiteral<Integer> typeLiteral1 = TypeLiteral.of(Integer.TYPE);
        TypeLiteral<Integer> typeLiteral2 = typeLiteral1;
        Object object = new Object();

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertNotEquals(typeLiteral1, object);
    }

    @Test
    public void instantiationEquivalence_List() {
        TypeLiteral<List<Integer>> typeLiteral1 = TypeLiteral.listOf(Integer.class);
        TypeLiteral<List<Integer>> typeLiteral2 = new TypeLiteral<List<Integer>>() {
        };
        TypeLiteral<?> typeLiteral3 = TypeLiteral.of(List.class, Integer.class);
        TypeLiteral<?> typeLiteral4 = TypeLiteral.listOf(TypeLiteral.of(Integer.class));

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertTypesEqual(typeLiteral1, typeLiteral3);
        assertTypesEqual(typeLiteral1, typeLiteral4);
        assertTypesEqual(typeLiteral2, typeLiteral3);
        assertTypesEqual(typeLiteral2, typeLiteral4);
        assertTypesEqual(typeLiteral3, typeLiteral4);
    }

    @Test
    public void instantiationEquivalence_Set() {
        TypeLiteral<Set<Integer>> typeLiteral1 = TypeLiteral.setOf(Integer.class);
        TypeLiteral<Set<Integer>> typeLiteral2 = new TypeLiteral<Set<Integer>>() {
        };
        TypeLiteral<?> typeLiteral3 = TypeLiteral.of(Set.class, Integer.class);
        TypeLiteral<?> typeLiteral4 = TypeLiteral.setOf(TypeLiteral.of(Integer.class));

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertTypesEqual(typeLiteral1, typeLiteral3);
        assertTypesEqual(typeLiteral1, typeLiteral4);
        assertTypesEqual(typeLiteral2, typeLiteral3);
        assertTypesEqual(typeLiteral2, typeLiteral4);
        assertTypesEqual(typeLiteral3, typeLiteral4);
    }

    @Test
    public void equalGenericsWithBounds() {
        TypeLiteral<List<? super Integer>> typeLiteral1 = new TypeLiteral<List<? super Integer>>() {
        };
        TypeLiteral<List<? super Integer>> typeLiteral2 = new TypeLiteral<List<? super Integer>>() {
        };
        TypeLiteral<List<? extends Integer>> typeLiteral3 = new TypeLiteral<List<? extends Integer>>() {
        };
        TypeLiteral<List<? extends Integer>> typeLiteral4 = new TypeLiteral<List<? extends Integer>>() {
        };

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertTypesEqual(typeLiteral3, typeLiteral4);
        assertTypesNotEqual(typeLiteral1, typeLiteral3);
        assertTypesNotEqual(typeLiteral2, typeLiteral4);
    }

    @Test
    public void equalArrays() {
        TypeLiteral<int[]> typeLiteral1 = new TypeLiteral<int[]>() {
        };
        TypeLiteral<int[]> typeLiteral2 = TypeLiteral.of(int[].class);
        TypeLiteral<Integer[]> typeLiteral3 = new TypeLiteral<Integer[]>() {
        };
        TypeLiteral<Integer[]> typeLiteral4 = TypeLiteral.of(Integer[].class);

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertTypesEqual(typeLiteral3, typeLiteral4);
        assertTypesNotEqual(typeLiteral1, typeLiteral3);
        assertTypesNotEqual(typeLiteral2, typeLiteral4);
    }

    @Test
    public void equalGenericsArrays() {
        TypeLiteral<List<Integer>[]> typeLiteral1 = new TypeLiteral<List<Integer>[]>() {
        };
        TypeLiteral<List<Integer>[]> typeLiteral2 = new TypeLiteral<List<Integer>[]>() {
        };
        TypeLiteral<List<Number>[]> typeLiteral3 = new TypeLiteral<List<Number>[]>() {
        };

        TypeLiteral<List<? super Integer>[]> typeLiteral4 = new TypeLiteral<List<? super Integer>[]>() {
        };
        TypeLiteral<List<? super Integer>[]> typeLiteral5 = new TypeLiteral<List<? super Integer>[]>() {
        };
        TypeLiteral<List<? super Number>[]> typeLiteral6 = new TypeLiteral<List<? super Number>[]>() {
        };

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertTypesNotEqual(typeLiteral1, typeLiteral3);
        assertTypesNotEqual(typeLiteral2, typeLiteral3);

        assertTypesEqual(typeLiteral4, typeLiteral5);
        assertTypesNotEqual(typeLiteral4, typeLiteral6);
        assertTypesNotEqual(typeLiteral5, typeLiteral6);
    }

    @Test
    public void nonEqualGenericsWithBounds() {
        TypeLiteral<List<Integer>> typeLiteral1 = new TypeLiteral<List<Integer>>() {
        };
        TypeLiteral<List<? super Integer>> typeLiteral2 = new TypeLiteral<List<? super Integer>>() {
        };
        TypeLiteral<List<? super Number>> typeLiteral3 = new TypeLiteral<List<? super Number>>() {
        };
        TypeLiteral<List<? extends Integer>> typeLiteral4 = new TypeLiteral<List<? extends Integer>>() {
        };
        TypeLiteral<List<? extends Number>> typeLiteral5 = new TypeLiteral<List<? extends Number>>() {
        };

        assertTypesNotEqual(typeLiteral1, typeLiteral2);
        assertTypesNotEqual(typeLiteral1, typeLiteral3);
        assertTypesNotEqual(typeLiteral1, typeLiteral4);
        assertTypesNotEqual(typeLiteral1, typeLiteral5);
        assertTypesNotEqual(typeLiteral2, typeLiteral3);
        assertTypesNotEqual(typeLiteral2, typeLiteral4);
        assertTypesNotEqual(typeLiteral2, typeLiteral5);
        assertTypesNotEqual(typeLiteral3, typeLiteral4);
        assertTypesNotEqual(typeLiteral3, typeLiteral5);
        assertTypesNotEqual(typeLiteral4, typeLiteral5);
    }

    @Test
    public void nestedGenerics() {
        TypeLiteral<Map<String, List<? extends Number>>> typeLiteral1 = new TypeLiteral<Map<String, List<? extends Number>>>() {
        };
        TypeLiteral<Map<String, List<? extends Number>>> typeLiteral2 = TypeLiteral.mapOf(new TypeLiteral<String>() {
        }, new TypeLiteral<List<? extends Number>>() {
        });
        TypeLiteral<Map<String, List<? extends Integer>>> typeLiteral3 = new TypeLiteral<Map<String, List<? extends Integer>>>() {
        };
        TypeLiteral<Map<String, List<? extends Integer>>> typeLiteral4 = TypeLiteral.mapOf(new TypeLiteral<String>() {
        }, new TypeLiteral<List<? extends Integer>>() {
        });

        assertTypesEqual(typeLiteral1, typeLiteral2);
        assertTypesEqual(typeLiteral3, typeLiteral4);

        assertTypesNotEqual(typeLiteral1, typeLiteral3);
        assertTypesNotEqual(typeLiteral1, typeLiteral4);
        assertTypesNotEqual(typeLiteral2, typeLiteral3);
        assertTypesNotEqual(typeLiteral2, typeLiteral4);
    }

    @Test
    public void normalize() {
        TypeLiteral<List<Integer>> type1 = TypeLiteral.listOf(Integer.class);
        TypeLiteral<List<Integer>> type2 = new TypeLiteral<List<Integer>>() {
        };
        TypeLiteral<List<Integer>> type3 = TypeLiteral.normalize(type1);
        TypeLiteral<List<Integer>> type4 = TypeLiteral.normalize(type2);

        assertTypesEqual(type1, type3);
        assertSame(type1, type3);

        assertTypesEqual(type2, type4);
        assertNotSame(type2, type4);
    }


    @Test
    public void creationFailure_NoGenericParam() {
        // No type parameters
        assertThrows(DIRuntimeException.class, () ->  new TypeLiteral() {
        });
    }

    @Test
    public void creationFailure_NoType() {
        // No type parameters
        assertThrows(NullPointerException.class, () -> TypeLiteral.of(null));
    }

    @Test
    public void variableTypeResolveFailure() {
        assertThrows(DIRuntimeException.class, () -> {
            TypeLiteral<List<Integer>> typeLiteral = genericMethodForTest();
        });
    }

    private static <T> TypeLiteral<List<T>> genericMethodForTest() {
        // Can't resolve variable type, should throw
        return new TypeLiteral<List<T>>() {
        };
    }

    private static void assertTypesEqual(TypeLiteral<?> literal1, TypeLiteral<?> literal2) {
        assertEquals(literal1.hashCode(), literal2.hashCode());
        assertEquals(literal1.toString(), literal2.toString());
        assertEquals(literal1, literal2);
    }

    private static void assertTypesNotEqual(TypeLiteral<?> literal1, TypeLiteral<?> literal2) {
        assertNotEquals(literal1.hashCode(), literal2.hashCode());
        assertNotEquals(literal1.toString(), literal2.toString());
        assertNotEquals(literal1, literal2);
    }
}
