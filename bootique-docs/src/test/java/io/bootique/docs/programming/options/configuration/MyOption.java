package io.bootique.docs.programming.options.configuration;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.option.ConfigResourceOption;
import io.bootique.option.ConfigValueOption;
import io.bootique.option.Option;

public class MyOption implements BQModule {

    @Override
    public void configure(Binder binder) {
// tag::OptionsConfig[]
        // Starting the app with "--my-opt=x" will set "jobs.myjob.param" value to "x"
        ConfigValueOption opt = Option.configValue("my-opt", "jobs.myjob.param").build();

        BQCoreModule.extend(binder).addOption(opt);
        // end::OptionsConfig[]


// tag::OptionsPredefined[]
        // Starting the app with "--my-opt" will set "jobs.myjob.param" value to "y"
        ConfigValueOption opt1 = Option.configValue("my-opt", "jobs.myjob.param")
                .valueOptionalWithDefault("y")
                .build();

        BQCoreModule.extend(binder).addOption(opt1);
        // end::OptionsPredefined[]


// tag::OptionsYaml[]
        // Starting the app with "--my-opt" is equivalent to starting with "--config=classpath:xyz.yml"
        ConfigResourceOption opt2 = Option.configResource("my-opt", "classpath:xyz.yml").build();

        BQCoreModule.extend(binder).addOption(opt2);
        // end::OptionsYaml[]

    }
}
