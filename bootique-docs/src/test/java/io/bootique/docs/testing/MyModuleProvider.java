package io.bootique.docs.testing;

import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;
import io.bootique.di.BQModule;
import io.bootique.jersey.JerseyModule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class MyModuleProvider implements BQModuleProvider {

    @Override
    public ModuleCrate moduleCrate() {
        BQModule module = b -> JerseyModule.extend(b).addResource(SomeResource.class);
        return ModuleCrate.of(module).build();
    }

    @Path("/somepath")
    public static class SomeResource {

        @GET
        public String hello() {
            return "{}";
        }
    }
}
