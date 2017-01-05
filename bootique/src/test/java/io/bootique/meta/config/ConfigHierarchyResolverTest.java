package io.bootique.meta.config;

import io.bootique.config.PolymorphicConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class ConfigHierarchyResolverTest {

    @Test
    public void testCreate_Classes() {
        ConfigHierarchyResolver resolver = ConfigHierarchyResolver
                .create(asList(Config4.class, Config3.class, Config2.class, Config1.class));

        assertEquals(Collections.emptySet(), resolver.directSubclasses(Object.class).collect(toSet()));
        assertEquals(new HashSet<>(asList(Config2.class, Config3.class)),
                resolver.directSubclasses(Config1.class).collect(toSet()));
    }

    @Test
    public void testCreate_BaseInterface() {
        ConfigHierarchyResolver resolver = ConfigHierarchyResolver
                .create(asList(Config5.class, Config6.class, IConfig1.class));

        assertEquals(new HashSet<>(asList(Config5.class, Config6.class)),
                resolver.directSubclasses(IConfig1.class).collect(toSet()));
    }


    public static interface IConfig1 extends PolymorphicConfiguration {

    }

    public static class Config1 implements PolymorphicConfiguration {

    }

    public static class Config2 extends Config1 {

    }

    public static class Config3 extends Config1 {

    }

    public static class Config4 extends Config2 {

    }

    public static class Config5 implements IConfig1 {

    }

    public static class Config6 implements IConfig1 {

    }

}
