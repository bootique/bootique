package io.bootique.di;

import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class BQInjectIT {

    @Test
    public void constructorInjection() {
        Injector injector = DIBootstrap.injectorBuilder(
                        b -> {
                            b.bind(Service.class).to(Service_Impl1.class);
                            b.bind(Consumer1.class);
                        })
                .build();

        Consumer1 consumer = injector.getInstance(Consumer1.class);
        assertInstanceOf(Service_Impl1.class, consumer.service);
    }

    @Test
    public void fieldInjection() {
        Injector injector = DIBootstrap.injectorBuilder(
                        b -> {
                            b.bind(Service.class).to(Service_Impl1.class);
                            b.bind(Consumer2.class);
                        })
                .build();

        Consumer2 consumer = injector.getInstance(Consumer2.class);
        assertInstanceOf(Service_Impl1.class, consumer.service);
    }


    interface Service {
        String doIt();
    }

    static class Service_Impl1 implements Service {
        @Override
        public String doIt() {
            return "impl1";
        }
    }

    static class Consumer1 {
        Service service;

        @BQInject
        Consumer1(Provider<Service> serviceProvider) {
            service = serviceProvider.get();
        }
    }

    static class Consumer2 {
        @BQInject
        Service service;
    }

}
