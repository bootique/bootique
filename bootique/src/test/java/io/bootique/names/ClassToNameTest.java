package io.bootique.names;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassToNameTest {

    @Test
    public void testStripSuffux() {

        ClassToName classToName = ClassToName.builder().convertToLowerCase().stripSuffix("Suffix").build();
        assertEquals("c1", classToName.toName(C1.class));
        assertEquals("c2", classToName.toName(C2Suffix.class));
        assertEquals("inner1static", classToName.toName(Inner1StaticSuffix.class));
        assertEquals("inner2", classToName.toName(Inner2Suffix.class));
    }

    @Test
    public void testLowerCase() {
        ClassToName classToName = ClassToName.builder().convertToLowerCase().build();
        assertEquals("c1", classToName.toName(C1.class));
    }

    @Test
    public void testPreserveCase() {
        ClassToName classToName = ClassToName.builder().build();
        assertEquals("C1", classToName.toName(C1.class));
    }

    @Test
    public void testSeparator() {
        ClassToName classToName = ClassToName.builder().partsSeparator("-").build();
        assertEquals("C3-Camel-Case", classToName.toName(C3CamelCase.class));
    }

    @Test
    public void testSeparatorAbbrevs() {
        ClassToName classToName = ClassToName.builder().partsSeparator("-").build();
        assertEquals("Cx-ABC", classToName.toName(CxABC.class));
    }

    static class Inner1StaticSuffix {
    }

    class Inner2Suffix {
    }
}

class C1 {
}

class C2Suffix {
}

class C3CamelCase {
}

class CxABC {
}

