package io.bootique.help.config;

import io.bootique.help.FormattedAppender;
import io.bootique.module.ModuleMetadata;
import io.bootique.module.ModulesMetadata;

import java.util.Collection;
import java.util.Objects;

/**
 * @since 0.21
 */
public class DefaultConfigHelpGenerator implements ConfigHelpGenerator {

    private ModulesMetadata modulesMetadata;
    private int lineWidth;

    public DefaultConfigHelpGenerator(ModulesMetadata modulesMetadata, int lineWidth) {
        this.lineWidth = lineWidth;
        this.modulesMetadata = modulesMetadata;
    }

    protected FormattedAppender createAppender(Appendable out) {
        return new FormattedAppender(out, lineWidth);
    }

    @Override
    public void append(Appendable out) {
        FormattedAppender appender = createAppender(out);
        printModules(appender, modulesMetadata.getModules());
    }

    protected void printModules(FormattedAppender out, Collection<ModuleMetadata> modules) {

        if (modules.isEmpty()) {
            return;
        }

        out.printSectionName("MODULES");
        modules.forEach(m -> {
            printModuleName(out, m.getName(), m.getDescription());
        });
    }

    protected void printModuleName(FormattedAppender out, String moduleName, String description) {
        Objects.requireNonNull(moduleName);

        if (description != null) {
            out.printSubsectionHeader(moduleName, ": ", description);
        } else {
            out.printSubsectionHeader(moduleName);
        }
    }
}
