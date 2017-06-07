package io.bootique;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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

    private List<Module> applyOverrides(Collection<RuntimeModule> modules) {
        return modules.stream()
                // find "roots"
                .filter(RuntimeModule::isNotOverridden)
                // fold overrides
                .map(this::merge)
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
                        o.isOverriddenBy(rm);
                        rm.overrides(o);
                    });
        }
    }

    private Module merge(RuntimeModule rm) {
        bootLogger.trace(() -> String.format("Adding module '%s' provided by '%s'...", rm.getModuleName(),
                rm.getProviderName()));

        Collection<RuntimeModule> overridden = rm.getOverridden();

        if (overridden.isEmpty()) {
            return rm.getModule();
        }

        Collection<Module> overrideModules = overridden.stream().map(o -> {
            bootLogger.trace(() -> String.format("Will override %s provided by %s...", o.getModuleName(),
                    o.getProviderName()));
            return o.getModule();
        }).collect(toList());

        return Modules.override(overrideModules).with(rm.getModule());
    }

}
