package io.bootique.cli.meta;

import io.bootique.Bootique;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CliApplicationTest {

    @Test
    public void testMainClass() {

        String mainClassName = CliApplication.mainClass();

        // TODO: until https://github.com/bootique/bootique/issues/52 is available,
        // we can't make an exact assertion here, as tests can be started from different IDEs, etc.

        Assert.assertNotNull(mainClassName);
        Assert.assertNotEquals(Bootique.class.getName(), mainClassName);
    }

    @Test
    public void testAppNameFromRuntime() {

        String name = CliApplication.appNameFromRuntime();

        // TODO: until https://github.com/bootique/bootique/issues/52 is available,
        // we can't make an exact assertion here, as tests can be started from different IDEs, etc.

        Assert.assertNotNull(name);
        assertEquals(-1, name.indexOf('.'));
        Assert.assertNotEquals(Bootique.class.getSimpleName(), name);
    }
}
