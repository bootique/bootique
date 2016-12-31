package io.bootique.meta.module;

import io.bootique.BQModule;
import io.bootique.meta.config.ConfigMetadataCompiler;
import io.bootique.meta.config.ConfigMetadataNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @since 0.21
 */
public class ModulesMetadataCompiler {

    private ConfigMetadataCompiler configCompiler;

    public ModulesMetadataCompiler(ConfigMetadataCompiler configCompiler) {
        this.configCompiler = configCompiler;
    }

    public ModulesMetadata compile(Collection<BQModule> modules) {
        ModulesMetadata.Builder builder = ModulesMetadata.builder();
        modules.forEach(m -> builder.addModule(toModuleMetadata(m)));
        return builder.build();
    }

    private ModuleMetadata toModuleMetadata(BQModule module) {
        return ModuleMetadata
                .builder(module.getName())
                .description(module.getDescription())
                .addConfigs(toConfigs(module))
                .build();
    }

    private Collection<ConfigMetadataNode> toConfigs(BQModule module) {

        Map<String, Class<?>> configTypes = module.getConfigs();
        if (configTypes.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<ConfigMetadataNode> configs = new ArrayList<>();

        configTypes.forEach((prefix, type) -> {
            configs.add(configCompiler.compile(prefix, type));
        });

        return configs;
    }
}
