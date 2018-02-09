package io.bootique;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utils methods which used inside {@link Bootique} class, but can be moved outside.
 *
 * @since 0.24
 */
final class BootiqueUtils {

    private BootiqueUtils() {
        throw new AssertionError("Should not be called.");
    }

    static Collection<BQModuleProvider> moduleProviderDependencies(Collection<BQModuleProvider> rootSet) {
        return moduleProviderDependencies(rootSet, new HashSet<>());
    }

    private static Collection<BQModuleProvider> moduleProviderDependencies(
            Collection<BQModuleProvider> rootSet,
            Set<BQModuleProvider> processed) {

        for (BQModuleProvider moduleProvider : rootSet) {
            if (!processed.contains(moduleProvider)) {
                processed.add(moduleProvider);

                final Collection<BQModuleProvider> dependencies = moduleProvider.dependencies();
                if (!dependencies.isEmpty()) {
                    processed.addAll(moduleProviderDependencies(dependencies, processed));
                }
            }
        }

        return processed;
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

