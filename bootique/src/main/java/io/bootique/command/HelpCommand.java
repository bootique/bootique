package io.bootique.command;

import com.google.inject.Provider;
import io.bootique.help.HelpGenerator;
import io.bootique.log.BootLogger;

/**
 * @deprecated since 0.21 moved to {@link io.bootique.help.HelpCommand}.
 */
@Deprecated
public class HelpCommand extends io.bootique.help.HelpCommand {

    public HelpCommand(BootLogger bootLogger, Provider<HelpGenerator> helpGeneratorProvider) {
        super(bootLogger, helpGeneratorProvider);
    }
}
