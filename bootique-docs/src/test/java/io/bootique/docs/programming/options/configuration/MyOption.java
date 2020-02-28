package io.bootique.docs.programming.options.configuration;

import io.bootique.BQCoreModule;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.meta.application.OptionMetadata;

public class MyOption implements BQModule {

    @Override
    public void configure(Binder binder) {
// tag::OptionsConfig[]
// Starting the app with "--my-opt=x" will set "jobs.myjob.param" value to "x"
        BQCoreModule.extend(binder)
                .addOption(OptionMetadata.builder("my-opt").build())
                .mapConfigPath("my-opt", "jobs.myjob.param");
        // end::OptionsConfig[]


// tag::OptionsPredefined[]
// Starting the app with "--my-opt" will set "jobs.myjob.param" value to "y"
        BQCoreModule.extend(binder)
                .addOption(OptionMetadata.builder("my-opt").valueOptionalWithDefault("y").build())
                .mapConfigPath("my-opt", "jobs.myjob.param");
        // end::OptionsPredefined[]


// tag::OptionsYaml[]
// Starting the app with "--my-opt" is equivalent to starting with "--config=classpath:xyz.yml"
        BQCoreModule.extend(binder)
                .addOption(OptionMetadata.builder("my-opt").build())
                .mapConfigResource("my-opt", "classpath:xyz.yml");
        // end::OptionsYaml[]

    }
}
