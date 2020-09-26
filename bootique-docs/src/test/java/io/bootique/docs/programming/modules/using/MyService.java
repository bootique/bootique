package io.bootique.docs.programming.modules.using;

import io.bootique.annotation.Args;

import javax.inject.Inject;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

// tag::MyService[]
public class MyService {

    @Args // <1>
    @Inject
    private String[] args;

    public String getArgsString() {
        return Stream.of(args).collect(joining(" "));
    }
}
// end::MyService[]

