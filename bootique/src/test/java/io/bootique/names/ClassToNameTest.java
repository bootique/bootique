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

package io.bootique.names;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("ABCCx", classToName.toName(ABCCx.class));
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

class ABCCx {
}
