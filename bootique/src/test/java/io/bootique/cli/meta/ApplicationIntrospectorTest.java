package io.bootique.cli.meta;

import io.bootique.Bootique;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationIntrospectorTest {

    @Test
    public void testMainClass() {

        Class<?> mainClass = ApplicationIntrospector.mainClass();

        // TODO: until https://github.com/bootique/bootique/issues/52 is available,
        // we can't make an exact assertion here, as tests can be started from different IDEs, etc.

        Assert.assertNotNull(mainClass);
        Assert.assertNotEquals(Bootique.class, mainClass);
    }

    @Test
    public void testAppNameFromRuntime() {

        String name = ApplicationIntrospector.appNameFromRuntime();

        // TODO: until https://github.com/bootique/bootique/issues/52 is available,
        // we can't make an exact assertion here, as tests can be started from different IDEs, etc.

        Assert.assertNotNull(name);
        Assert.assertNotEquals("Bootique", name);
    }
}
