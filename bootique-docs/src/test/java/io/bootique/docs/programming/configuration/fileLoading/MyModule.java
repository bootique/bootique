package io.bootique.docs.programming.configuration.fileLoading;

import io.bootique.BQCoreModule;
import io.bootique.BaseModule;
import io.bootique.di.Binder;
import io.bootique.meta.application.OptionMetadata;

public class MyModule extends BaseModule {

    @Override
    public void configure(Binder binder) {

        // tag::MyModuleExtendBinder[]
        BQCoreModule.extend(binder).addConfig("classpath:com/foo/default.yml");
        // end::MyModuleExtendBinder[]


        // tag::MyModuleQAOption[]
        OptionMetadata o = OptionMetadata.builder("qa")
                .description("when present, uses QA config")
                .build();

        BQCoreModule.extend(binder)
                .addOption(o)
                .mapConfigResource(o.getName(), "classpath:a/b/qa.yml");
        // end::MyModuleQAOption[]


        // tag::MyModuleDBOption[]
        OptionMetadata o1 = OptionMetadata.builder("db")
                .description("specifies database URL")
                .valueOptionalWithDefault("jdbc:mysql://127.0.0.1:3306/mydb")
                .build();

        BQCoreModule.extend(binder)
                .addOption(o1)
                .mapConfigPath(o1.getName(), "jdbc.mydb.url");
        // end::MyModuleDBOption[]

    }
}

