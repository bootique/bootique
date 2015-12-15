[![Build Status](https://travis-ci.org/nhl/bootique.svg)](https://travis-ci.org/nhl/bootique)

Bootique is a minimally opinionated technology for building single-jar runnable Java applications of any kind. With Bootique you can create and run REST services, webapps, jobs, etc. as if they were simple commands. No JEE container required!

Bootique was inspired by two similar products - [Dropwizard](http://www.dropwizard.io) and [SpringBoot](http://projects.spring.io/spring-boot/), however its focus is different. Bootique favors modularity and clean pluggable architecture.

Bootique is built on top of [Google Guice](https://github.com/google/guice) DI container, that is the core of its modularity mechanism.

## Getting Started

Declare Bootique Maven repository in your pom.xml (unless you have your own repo proxy, in which case add this repo to the proxy):

```XML
<repositories>
    <repository>
        <id>lm-repo</id>
        <name>ObjectStyle LinkMove Repo</name>
        <url>http://maven.objectstyle.org/nexus/content/repositories/bootique</url>
    </repository>
</repositories>
```

_TODO: eventually we'll start publishing Bootique to Central, so the step above will not be needed._

## YAML Config

## YAML Config Property Overrides
