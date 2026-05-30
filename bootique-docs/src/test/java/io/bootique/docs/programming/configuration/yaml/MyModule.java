package io.bootique.docs.programming.configuration.yaml;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.docs.programming.configuration.MyService;
import io.bootique.option.ConfigResourceOption;
import io.bootique.option.ConfigValueOption;
import io.bootique.option.Option;
import jakarta.inject.Singleton;

// tag::MyModuleConfig[]
public class MyModule implements BQModule {

    @Singleton
    @Provides
    public MyService createMyService(
            ConfigurationFactory configFactory,
            SomeOtherService service) {

        return configFactory.config(MyFactory.class, "my").createMyService(service);
    }
    // end::MyModuleConfig[]

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
    // tag::MyModuleConfig[]
}
// end::MyModuleConfig[]

