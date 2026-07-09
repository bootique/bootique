package io.bootique.docs.programming.configuration.fileLoading;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.option.ConfigResourceOption;
import io.bootique.option.ConfigValueOption;
import io.bootique.option.Option;

public class MyModule implements BQModule {

    @Override
    public void configure(Binder binder) {

        // tag::MyModuleExtendBinder[]
        BQCoreModule.extend(binder).addConfig("classpath:com/foo/default.yml");
        // end::MyModuleExtendBinder[]


        // tag::MyModuleQAOption[]
        ConfigResourceOption o = Option.configResource("qa", "classpath:a/b/qa.yml")
                .description("when present, uses QA config")
                .build();

        BQCoreModule.extend(binder).addOption(o);
        // end::MyModuleQAOption[]


        // tag::MyModuleDBOption[]
        ConfigValueOption o1 = Option.configValue("db", "jdbc.mydb.url")
                .description("specifies database URL")
                .valueOptionalWithDefault("jdbc:mysql://127.0.0.1:3306/mydb")
                .build();

        BQCoreModule.extend(binder).addOption(o1);
        // end::MyModuleDBOption[]

    }
}

