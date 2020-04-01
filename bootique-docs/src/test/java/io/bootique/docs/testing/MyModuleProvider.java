package io.bootique.docs.testing;

import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;
import io.bootique.jersey.JerseyModule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class MyModuleProvider implements BQModuleProvider {

    @Override
    public BQModule module() {
        return b -> JerseyModule.extend(b).addResource(SomeResource.class);
    }

    @Path("/somepath")
    public static class SomeResource {

        @GET
        public String hello() {
            return "{}";
        }
    }
}
