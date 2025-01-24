package io.bootique.di;

import io.bootique.BQModule;
import jakarta.inject.Qualifier;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChainedBindingsIT {

    @Test
    public void chainedBindingDirect() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Service.class).to(SubService.class);
            binder.bind(SubService.class).to(ServiceImpl.class);
        }).build();

        Service service = injector.getInstance(Service.class);
        assertNotNull(service);
        assertEquals("ServiceImpl", service.doSomething());
    }

    @Test
    public void chainedBindingProvideMethod() {
        Injector injector = DIBootstrap
                .injectorBuilder(new MainModule(), new SubModule())
                .build();

        Service service = injector.getInstance(Key.get(Service.class, TestQualifier.class));
        assertNotNull(service);
        assertEquals("ServiceImpl", service.doSomething());
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestQualifier {
    }

    static class MainModule implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(Service.class, TestQualifier.class)
                    .to(SubService.class)
                    .inSingletonScope();
        }
    }

    static class SubModule implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public SubService createService() {
            return new ServiceImpl();
        }
    }

    interface Service {
        String doSomething();
    }

    interface SubService extends Service {
    }

    static class ServiceImpl implements SubService {
        @Override
        public String doSomething() {
            return "ServiceImpl";
        }
    }
}
