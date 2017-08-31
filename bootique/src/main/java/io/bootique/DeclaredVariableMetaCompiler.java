package io.bootique;

import io.bootique.env.DeclaredVariable;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Optional;

class DeclaredVariableMetaCompiler {

    static Optional<ConfigValueMetadata> compileIfValid(DeclaredVariable var, ModulesMetadata modulesMetadata) {

        for (ModuleMetadata mm : modulesMetadata.getModules()) {
            Optional<ConfigMetadataNode> cmn = mm.findConfig(var.getConfigPath());
            if (cmn.isPresent()) {
                return cmn.map(n -> compileMetadata(var, n));
            }
        }

        return Optional.empty();
    }

    private static ConfigValueMetadata compileMetadata(DeclaredVariable variable, ConfigMetadataNode configMetadata) {

        // TODO: validation... verify that the variable is bound to a value, not a collection or a map??

        return ConfigValueMetadata
                .builder(variable.getName())
                .description(configMetadata.getDescription())
                .type(configMetadata.getType()).build();
    }
}
