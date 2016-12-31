package io.bootique.meta.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provides API to look up subclasses of a given class within a predefined set of types.
 *
 * @since 0.21
 */
public class ConfigHierarchyResolver {

    private Map<Class<?>, Type> hierarchies;

    protected ConfigHierarchyResolver(Map<Class<?>, Type> hierarchies) {
        this.hierarchies = hierarchies;
    }

    public static <T> ConfigHierarchyResolver create(Collection<Class<? extends T>> typeSet) {

        Map<Class<?>, Type> hierarchies = new HashMap<>();

        // Types are graph nodes
        typeSet.forEach(c -> hierarchies.put(c, new Type(c)));

        // subclass relationship are graph edges
        hierarchies.values().forEach(t -> {

            Class<?> superClass = t.getRootClass().getSuperclass();
            while (superClass != null) {
                Type superType = hierarchies.get(superClass);
                if (superType != null) {
                    superType.getDirectSubclasses().add(t);
                    break;
                }

                superClass = superClass.getSuperclass();
            }
        });

        return new ConfigHierarchyResolver(hierarchies);
    }

    public Stream<Class<?>> directSubclasses(Class<?> superclass) {
        Type type = hierarchies.get(superclass);
        return (type == null) ? Stream.empty() : type.getDirectSubclasses().stream().map(Type::getRootClass);
    }

    static class Type {
        private Class<?> rootClass;
        private Collection<Type> directSubclasses;

        public Type(Class<?> rootClass) {
            this.rootClass = rootClass;
            this.directSubclasses = new HashSet<>();
        }

        public Collection<Type> getDirectSubclasses() {
            return directSubclasses;
        }

        public Class<?> getRootClass() {
            return rootClass;
        }
    }
}
