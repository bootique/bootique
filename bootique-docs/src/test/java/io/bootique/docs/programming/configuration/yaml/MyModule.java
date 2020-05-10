package io.bootique.docs.programming.configuration.yaml;

import io.bootique.BQCoreModule;
import io.bootique.BaseModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.docs.programming.configuration.MyService;
import io.bootique.meta.application.OptionMetadata;

import javax.inject.Singleton;

// tag::MyModuleConfig[]
public class MyModule extends BaseModule {

    @Singleton
    @Provides
    public MyService createMyService(
            ConfigurationFactory configFactory,
            SomeOtherService service) {

        return config(MyFactory.class, configFactory).createMyService(service);
    }
    // end::MyModuleConfig[]

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
    // tag::MyModuleConfig[]
}
// end::MyModuleConfig[]

