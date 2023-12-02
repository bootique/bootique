package io.bootique.di.spi;

import io.bootique.di.TypeLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericTypesUtilsTest {

    Field field1;
    Field field2;
    Field field3;
    Field field6;
    Field field7;
    Field field8;
    Field field9;

    @BeforeEach
    public void initFields() throws NoSuchFieldException {
        field1 = Service1.class.getDeclaredField("field1");
        field2 = Service2.class.getDeclaredField("field2");
        field3 = Service2.class.getDeclaredField("field3");
        field6 = Service5.class.getDeclaredField("field6");
        field7 = Service5.class.getDeclaredField("field7");
        field8 = Service5.class.getDeclaredField("field8");
        field9 = Service5.class.getDeclaredField("field9");
    }

    @Test
    public void service2() {
        TypeLiteral<?> typeLiteral = GenericTypesUtils.resolveVariableType(Service2.class, field1, field1.getGenericType());
        assertEquals(String.class, typeLiteral.getRawType());
    }

    @Test
    public void service3() {
        TypeLiteral<?> typeLiteral1 = GenericTypesUtils.resolveVariableType(Service3.class, field1, field1.getGenericType());
        assertEquals(String.class, typeLiteral1.getRawType());

        TypeLiteral<?> typeLiteral2 = GenericTypesUtils.resolveVariableType(Service3.class, field2, field2.getGenericType());
        assertEquals(Integer.class, typeLiteral2.getRawType());

        TypeLiteral<?> typeLiteral3 = GenericTypesUtils.resolveVariableType(Service3.class, field3, field3.getGenericType());
        assertEquals(Long.class, typeLiteral3.getRawType());
    }

    @Test
    public void service4() {
        TypeLiteral<?> typeLiteral1 = GenericTypesUtils.resolveVariableType(Service4.class, field1, field1.getGenericType());
        assertEquals(String.class, typeLiteral1.getRawType());

        TypeLiteral<?> typeLiteral2 = GenericTypesUtils.resolveVariableType(Service4.class, field2, field2.getGenericType());
        assertEquals(Float.class, typeLiteral2.getRawType());

        TypeLiteral<?> typeLiteral3 = GenericTypesUtils.resolveVariableType(Service4.class, field3, field3.getGenericType());
        assertEquals(Double.class, typeLiteral3.getRawType());
    }

    @Test
    public void service6() {
        TypeLiteral<?> typeLiteral1 = GenericTypesUtils.resolveVariableType(Service6.class, field6, field6.getGenericType());
        assertEquals(String.class, typeLiteral1.getRawType());

        TypeLiteral<?> typeLiteral2 = GenericTypesUtils.resolveVariableType(Service6.class, field7, field7.getGenericType());
        assertEquals(Integer.class, typeLiteral2.getRawType());

        TypeLiteral<?> typeLiteral3 = GenericTypesUtils.resolveVariableType(Service6.class, field8, field8.getGenericType());
        assertEquals(Long.class, typeLiteral3.getRawType());

        TypeLiteral<?> typeLiteral4 = GenericTypesUtils.resolveVariableType(Service6.class, field9, field9.getGenericType());
        assertEquals(Character.class, typeLiteral4.getRawType());
    }

    static class Service1<A> {
        A field1;
    }

    static class Service2<B,C> extends Service1<String> {
        B field2;
        int field;
        C field3;
    }

    static class Service3 extends Service2<Integer,Long> {
    }

    static class Service4<D, E> extends Service2<Float,Double> {
        int _f;
        D field4;
        int __f;
        E field5;
    }

    static class Service5<A,B,C,D> {
        int _f;
        A field6;
        B field7;
        C field8;
        int __f;
        D field9;
    }

    static class Service6 extends Service5<String, Integer, Long, Character> {}
}