package io.bootique.test.junit5;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.log.DefaultBootLogger;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolymorphicConfigurationChecker<T extends PolymorphicConfiguration> {

    private Class<T> expectedRoot;
    private Class<? extends T> expectedDefault;
    private Set<Class<? extends T>> allExpectedTypes;

    @SafeVarargs
    protected PolymorphicConfigurationChecker(
            Class<T> expectedRoot,
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
            for (Class<? extends T> c : otherConfigs) {
                allTypes.add(c);
            }
        }

        this.allExpectedTypes = allTypes;
    }

    @SafeVarargs
    public static <T extends PolymorphicConfiguration> void test(
            Class<T> expectedRoot,
            Class<? extends T> expectedDefault,
            Class<? extends T>... otherConfigs) {
        new PolymorphicConfigurationChecker<>(expectedRoot, expectedDefault, otherConfigs).test();
    }

    @SafeVarargs
    public static <T extends PolymorphicConfiguration> void testNoDefault(
            Class<T> expectedRoot,
            Class<? extends T>... otherConfigs) {
        new PolymorphicConfigurationChecker<>(expectedRoot, null, otherConfigs).test();
    }

    protected void test() {

        Set<Class<? extends PolymorphicConfiguration>> loaded = loadedFromSpi();
        assertEquals(allExpectedTypes, loaded, "Loaded and expected types do not match");

        testRoot();

        allExpectedTypes.forEach(t -> {

            if (!t.equals(expectedRoot)) {
                testNonRoot(t);
            }
        });
    }

    protected void testRoot() {
        // while boundaries are compiler-checked, let's still verify superclass, as generics in Java are easy to bypass
        assertTrue(PolymorphicConfiguration.class.isAssignableFrom(expectedRoot), "Invalid root type: " + expectedRoot);

        JsonTypeInfo typeInfo = expectedRoot.getAnnotation(JsonTypeInfo.class);

        //  TODO: test "property" and "use" values of the annotation
        assertNotNull(typeInfo,"Root is not annotated with @JsonTypeInfo");
        if (expectedDefault != null) {
            assertTrue(hasDefault(typeInfo),
                    "Default type is not specified on root. Expected: " + expectedDefault.getName());
            assertEquals(expectedDefault, typeInfo.defaultImpl(),
                    "Expected and actual default types are not the same");
        } else {
            assertFalse(hasDefault(typeInfo),
                    "Expected no default type, but @JsonTypeInfo sets it to " + typeInfo.defaultImpl().getName() + ".");
        }

        if (isConcrete(expectedRoot)) {
            JsonTypeName typeName = expectedRoot.getAnnotation(JsonTypeName.class);
            assertNotNull(typeName,"Concrete root configuration type must be annotated with @JsonTypeName: " + expectedRoot.getName());
        }
    }

    protected void testNonRoot(Class<? extends T> t) {
        // while boundaries are compiler-checked, let's still verify superclass, as generics in Java are easy to bypass
        assertTrue(expectedRoot.isAssignableFrom(t),
                "Invalid type " + t.getName() + ". Must be a subclass of root type " + expectedRoot.getName());

        assertTrue(isConcrete(t), "Non-root configuration type must not be abstract: " + t.getName());

        // this check would prevent matching subclasses by class, but we discourage that anyways.. (otherwise FQN
        // would have to be used in YAML)

        JsonTypeName typeName = t.getAnnotation(JsonTypeName.class);
        assertNotNull(typeName,"Non-root configuration type must be annotated with @JsonTypeName: " + t.getName());
    }


    protected Set<Class<? extends PolymorphicConfiguration>> loadedFromSpi() {

        Collection<Class<? extends PolymorphicConfiguration>> types;
        try {
            types = new TypesFactory<>(
                    getClass().getClassLoader(),
                    PolymorphicConfiguration.class,
                    new DefaultBootLogger(false)).getTypes();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
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
        return !typeInfo.defaultImpl().equals(JsonTypeInfo.class);
    }
}
