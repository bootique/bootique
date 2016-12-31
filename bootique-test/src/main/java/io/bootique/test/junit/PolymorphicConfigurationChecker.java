package io.bootique.test.junit;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.log.DefaultBootLogger;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A helper to test {@link PolymorphicConfiguration} hierarchies.
 *
 * @param <T> the type of the root of configuration.
 */
public class PolymorphicConfigurationChecker<T extends PolymorphicConfiguration> {

    private Class<T> expectedRoot;
    private Class<? extends T> expectedDefault;
    private Set<Class<? extends T>> allExpectedTypes;

    protected PolymorphicConfigurationChecker(Class<T> expectedRoot,
                                              Class<? extends T> expectedDefault,
                                              Class<? extends T>... otherConfigs) {
        this.expectedRoot = Objects.requireNonNull(expectedRoot);
        this.expectedDefault = expectedDefault;

        Set<Class<? extends T>> allTypes = new HashSet<>();
        allTypes.add(Objects.requireNonNull(expectedRoot));

        if (expectedDefault != null) {
            allTypes.add(expectedDefault);
        }

        if (otherConfigs != null) {
            allTypes.addAll(asList(otherConfigs));
        }

        this.allExpectedTypes = allTypes;
    }

    public static <T extends PolymorphicConfiguration> void test(Class<T> expectedRoot,
                                                                 Class<? extends T> expectedDefault,
                                                                 Class<? extends T>... otherConfigs) {
        new PolymorphicConfigurationChecker<>(expectedRoot, expectedDefault, otherConfigs).test();
    }

    public static <T extends PolymorphicConfiguration> void testNoDefault(Class<T> expectedRoot,
                                                                          Class<? extends T>... otherConfigs) {
        new PolymorphicConfigurationChecker<>(expectedRoot, null, otherConfigs).test();
    }

    protected void test() {

        Set<Class<? extends PolymorphicConfiguration>> loaded = loadedFromSpi();
        assertEquals("Loaded and expected types do not match", allExpectedTypes, loaded);

        testRoot();

        allExpectedTypes.forEach(t -> {

            if (!t.equals(expectedRoot)) {
                testNonRoot(t);
            }
        });
    }

    protected void testRoot() {
        // while boundaries are compiler-checked, let's still verify superclass, as generics in Java are easy to bypass
        assertTrue("Invalid root type: " + expectedRoot, PolymorphicConfiguration.class.isAssignableFrom(expectedRoot));

        JsonTypeInfo typeInfo = expectedRoot.getAnnotation(JsonTypeInfo.class);

        //  TODO: test "property" and "use" values of the annotation
        assertNotNull("Root is not annotated with @JsonTypeInfo", typeInfo);
        if (expectedDefault != null) {
            assertTrue("Default type is not specified on root. Expected: " + expectedDefault.getName(),
                    hasDefault(typeInfo));
            assertEquals("Expected and actual default types are not the same", expectedDefault,
                    typeInfo.defaultImpl());
        } else {
            assertFalse("Expected no default type, but @JsonTypeInfo sets it to " + typeInfo.defaultImpl().getName() + ".",
                    hasDefault(typeInfo));
        }

        if (isConcrete(expectedRoot)) {
            JsonTypeName typeName = expectedRoot.getAnnotation(JsonTypeName.class);
            assertNotNull("Concrete root configuration type must be annotated with @JsonTypeName: " + expectedRoot.getName());
        }
    }

    protected void testNonRoot(Class<? extends T> t) {
        // while boundaries are compiler-checked, let's still verify superclass, as generics in Java are easy to bypass
        assertTrue("Invalid type " + t.getName() + ". Must be a subclass of root type " + expectedRoot.getName(),
                expectedRoot.isAssignableFrom(t));

        assertTrue("Non-root configuration type must not be abstract: " + t.getName(), isConcrete(t));

        // this check would prevent matching subclasses by class, but we discourage that anyways.. (otherwise FQN
        // would have to be used in YAML)

        JsonTypeName typeName = t.getAnnotation(JsonTypeName.class);
        assertNotNull("Non-root configuration type must be annotated with @JsonTypeName: " + t.getName());
    }


    protected Set<Class<? extends PolymorphicConfiguration>> loadedFromSpi() {

        Collection<Class<? extends PolymorphicConfiguration>> types;
        try {
            types = new TypesFactory<>(
                    getClass().getClassLoader(),
                    PolymorphicConfiguration.class,
                    new DefaultBootLogger(false)).getTypes();
        } catch (Exception e) {
            fail(e.getMessage());
            // dead code; still required to compile...
            throw new RuntimeException(e);
        }

        return types.stream()
                .filter(p -> expectedRoot.isAssignableFrom(p))
                .collect(toSet());
    }

    protected boolean isConcrete(Class<?> type) {
        int modifiers = type.getModifiers();
        return !Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers);
    }

    protected boolean hasDefault(JsonTypeInfo typeInfo) {
        // weak... esp. since None is deprecated in JsonTypeInfo
        return !typeInfo.defaultImpl().getName().endsWith("JsonTypeInfo$None");
    }
}
