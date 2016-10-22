package io.bootique;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class RuntimeModuleMerger {

    private BootLogger bootLogger;

    RuntimeModuleMerger(BootLogger bootLogger) {
        this.bootLogger = bootLogger;
    }

    List<Module> toGuiceModules(Collection<BQModule> bqModules) {
        Map<Class<? extends Module>, RuntimeModule> moduleMap = map(bqModules);
        return merge(moduleMap);
    }

    private Map<Class<? extends Module>, RuntimeModule> map(Collection<BQModule> bqModules) {
        Map<Class<? extends Module>, RuntimeModule> nodes = toNodeMap(bqModules);

        calcOverrides(nodes);
        calcCycles(nodes);

        return nodes;
    }

    private List<Module> merge(Map<Class<? extends Module>, RuntimeModule> nodes) {

        Predicate<RuntimeModule> notOverridden = n -> {
            if (n.getOverriddenBy().isEmpty()) {
                return true;
            }

            if (n.getOverriddenBy().size() == 1) {
                return false;
            }

            String overrideList = n.getOverriddenBy().stream().map(RuntimeModule::getModuleName)
                    .collect(joining(", "));
            String message = String.format(
                    "Module %s provided by %s is overridden more then once. Overriding modules: %s",
                    n.getModuleName(), n.getProviderName(), overrideList);
            throw new RuntimeException(message);
        };

        Function<RuntimeModule, Module> toModule = (n) -> {

            bootLogger.trace(() -> String.format("Adding module '%s' provided by '%s'...", n.getModuleName(),
                    n.getProviderName()));

            Collection<RuntimeModule> overrides = n.getModuleOverrides(nodes);

            if (overrides.isEmpty()) {
                return n.getModule();
            }

            Collection<Module> overrideModules = overrides.stream().map(o -> {
                bootLogger.trace(() -> String.format("Will override %s provided by %s...", o.getModuleName(),
                        o.getProviderName()));
                return o.getModule();
            }).collect(toList());

            return Modules.override(overrideModules).with(n.getModule());

        };

        return nodes.values().stream().filter(notOverridden).map(toModule).collect(toList());
    }


    private Map<Class<? extends Module>, RuntimeModule> toNodeMap(Collection<BQModule> bqModules) {

        // TODO: looking up modules by java type limits the use of lambdas as modules. E.g. we loaded test
        // properties are dynamically created modules in a repeatedly called Lambda. This didn't work..
        // So perhaps use provider name as a unique key?

        Map<Class<? extends Module>, RuntimeModule> nodes = new LinkedHashMap<>();

        bqModules.forEach(m -> {

            RuntimeModule rm = new RuntimeModule(m);
            RuntimeModule existing = nodes.putIfAbsent(rm.getModuleType(), rm);
            if (existing != null) {
                bootLogger.trace(() -> String.format(
                        "Skipping module '%s' provided by '%s' (already provided by '%s')...",
                        rm.getModuleName(),
                        rm.getProviderName(),
                        existing.getProviderName()));
            }
        });

        return nodes;
    }

    private void calcOverrides(Map<Class<? extends Module>, RuntimeModule> nodes) {
        nodes.values().forEach(n -> {
            n.getModuleOverrides(nodes).forEach(o -> o.addOverriddenBy(n));
        });
    }

    private void calcCycles(Map<Class<? extends Module>, RuntimeModule> nodes) {
        nodes.values().forEach(n -> {
            n.checkCycles();
        });
    }
}
