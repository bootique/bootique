package io.bootique;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Utils methods which used inside {@link Bootique} class, but can be moved outside.
 *
 * @since 0.24
 */
final class BootiqueUtils {

    private BootiqueUtils() {
        throw new AssertionError("Should not be called.");
    }

    /**
     * Load dependency graph of {@link BQModuleProvider}s.
     *
     * 1. Add rootSet to result collection;
     * 2. For each {@link BQModuleProvider} in root set recursive load their dependent {@link BQModuleProvider}s;
     *
     * @param rootSet {@link BQModuleProvider} contributed through auto-loading and Bootique builder.
     * @return root set and their dependency graph.
     */
    static Collection<BQModuleProvider> moduleProviderDependencies(Collection<BQModuleProvider> rootSet) {
        return moduleProviderDependencies(rootSet, new HashMap<>())
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    private static Map<BQModuleId, BQModuleProvider> moduleProviderDependencies(
            final Collection<BQModuleProvider> moduleProviders,
            final Map<BQModuleId, BQModuleProvider> processed) {

        moduleProviders.forEach(moduleProvider -> processed.put(moduleProvider.id(), moduleProvider));

        for (BQModuleProvider moduleProvider : moduleProviders) {
            final Collection<Class<? extends BQModuleProvider>> dependencies = moduleProvider.dependencies();

            if (!dependencies.isEmpty()) {
                final List<BQModuleProvider> notProcessedDependencies = dependencies
                        .stream()
                        .map(BootiqueUtils::createBQModuleProvider)
                        .filter(provider -> !processed.containsKey(provider.id()))
                        .collect(toList());

                moduleProviderDependencies(notProcessedDependencies, processed);
            }
        }

        return processed;
    }

    private static <T extends BQModuleProvider> T createBQModuleProvider(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error instantiating BQModuleProvider of type: " + clazz.getName(), e);
        }
    }

    static String[] mergeArrays(String[] a1, String[] a2) {
        if (a1.length == 0) {
            return a2;
        }

        if (a2.length == 0) {
            return a1;
        }

        String[] merged = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, merged, 0, a1.length);
        System.arraycopy(a2, 0, merged, a1.length, a2.length);

        return merged;
    }

    static String[] toArray(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }
}

