package io.bootique.docs.programming.modules.using;

import io.bootique.annotation.Args;

import javax.inject.Inject;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

// tag::MyService[]
public class MyService {

    @Args
    @Inject
    private String[] args;

    public String getArgsString() {
        return Arrays.asList(getArgs()).stream().collect(joining(" "));
    }
    // end::MyService[]

    private String getArgs() {
        return null;
    }
    // tag::MyService[]
}
// end::MyService[]

