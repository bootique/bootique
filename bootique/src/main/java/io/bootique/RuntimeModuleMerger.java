package io.bootique;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class RuntimeModuleMerger {

    private BootLogger bootLogger;

    RuntimeModuleMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    Collection<Module> toGuiceModules(Collection<BQModule> bqModules) {
        return applyOverrides(checkCycles(collectUnique(bqModules)));
    }

    private Collection<Module> applyOverrides(Collection<RuntimeModule> modules) {
        return modules.stream()
                // find "heads" in override dependency linked lists.
                .filter(RuntimeModule::doesNotOverrideOthers)
                // fold each overrides linked list into a single module
                .map(this::fold)
                .collect(toList());
    }

    private Collection<RuntimeModule> checkCycles(Collection<RuntimeModule> modules) {
        modules.forEach(RuntimeModule::checkCycles);
        return modules;
    }

    private Collection<RuntimeModule> collectUnique(Collection<BQModule> bqModules) {

        // TODO: looking up modules by java type limits the use of lambdas as modules. E.g. we loaded test
        // properties are dynamically created modules in a repeatedly called Lambda. This didn't work..
        // So perhaps use provider name as a unique key?

        Map<Class<? extends Module>, RuntimeModule> map = new LinkedHashMap<>();

        for (BQModule bqModule : bqModules) {

            RuntimeModule rm = new RuntimeModule(bqModule);

            RuntimeModule existing = map.putIfAbsent(rm.getModuleType(), rm);
            if (existing != null) {
                bootLogger.trace(() -> String.format(
                        "Skipping module '%s' provided by '%s' (already provided by '%s')...",
                        rm.getModuleName(),
                        rm.getProviderName(),
                        existing.getProviderName()));
            }
        }

        calcOverrideGraph(map);

        return map.values();
    }

    private void calcOverrideGraph(Map<Class<? extends Module>, RuntimeModule> modules) {

        for (RuntimeModule rm : modules.values()) {
            rm.getBqModule()
                    .getOverrides()
                    .stream()
                    .map(t -> modules.get(t))
                    .filter(rmn -> rmn != null)
                    .forEach(o -> {
                        o.setOverriddenBy(rm);
                        rm.setOverridesOthers(true);
                    });
        }
    }

    private Module fold(RuntimeModule rm) {
        bootLogger.trace(() ->
                String.format("Adding module '%s' provided by '%s'...", rm.getModuleName(),
                        rm.getProviderName()));

        RuntimeModule overriddenBy = rm.getOverriddenBy();

        if (overriddenBy == null) {
            return rm.getModule();
        }

        bootLogger.trace(() -> String.format("Will override %s provided by %s...",
                rm.getModuleName(),
                rm.getProviderName()));

        return Modules.override(rm.getModule()).with(fold(overriddenBy));
    }

}
