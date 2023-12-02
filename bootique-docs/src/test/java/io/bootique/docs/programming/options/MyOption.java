package io.bootique.docs.programming.options;

import io.bootique.BQCoreModule;
import io.bootique.cli.Cli;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.meta.application.OptionMetadata;

import javax.inject.Inject;
import java.util.Collection;

public class MyOption implements BQModule {

    // tag::Options[]
    OptionMetadata option = OptionMetadata
            .builder("email", "An admin email address")
            .valueRequired("email_address")
            .build();

    // end::Options[]

    @Override
    public void configure(Binder binder) {
        // tag::Options[]
        BQCoreModule.extend(binder).addOption(option);
        // end::Options[]
    }

    // tag::OptionsInject[]
    @Inject
    private Cli cli;

    public void doSomething() {
        Collection<String> emails = cli.optionStrings("email");
        // do something with option values....
    }
    // end::OptionsInject[]

}
