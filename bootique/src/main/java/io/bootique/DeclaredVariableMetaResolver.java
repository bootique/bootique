package io.bootique;

import io.bootique.env.DeclaredVariable;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

class DeclaredVariableMetaResolver {

    private ModulesMetadata modulesMetadata;

    public DeclaredVariableMetaResolver(ModulesMetadata modulesMetadata) {
        this.modulesMetadata = modulesMetadata;
    }

    public Stream<ConfigValueMetadata> resolve(Collection<DeclaredVariable> vars) {
        return vars.stream()
                .map(this::resolve)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    Optional<ConfigValueMetadata> resolve(DeclaredVariable var) {
        return modulesMetadata.getModules()
                .stream()
                .map(m -> m.findConfig(var.getConfigPath()))
                .filter(Optional::isPresent)
                .map(o -> compileMetadata(var, o.get()))
                .findFirst();
    }

    ConfigValueMetadata compileMetadata(DeclaredVariable variable, ConfigMetadataNode configMetadata) {

        // TODO: validation... verify that the variable is bound to a value, not a collection or a map??

        return ConfigValueMetadata
                .builder(variable.getName())
                .description(configMetadata.getDescription())
                .type(configMetadata.getType()).build();
    }
}
