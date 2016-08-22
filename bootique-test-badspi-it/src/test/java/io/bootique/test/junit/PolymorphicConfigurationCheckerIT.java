package io.bootique.test.junit;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.junit.Test;

public class PolymorphicConfigurationCheckerIT {

    @Test(expected = AssertionError.class)
    public void test_NotPolymorphicConfiguration() {

        // intentionally tricking Java type boundary checks
        Class c1 = C1.class;
        Class c2 = C2.class;
        PolymorphicConfigurationChecker.test(c1, c2);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = C2.class)
    public static class C1 {
    }

    @JsonTypeName("c2")
    public static class C2 extends C1 {
    }
}
