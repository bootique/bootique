[![Build Status](https://travis-ci.org/nhl/bootique.svg)](https://travis-ci.org/nhl/bootique)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.nhl.bootique/bootique/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.nhl.bootique/bootique/)

Bootique is a [minimally opinionated](https://medium.com/@andrus_a/bootique-a-minimally-opinionated-platform-for-modern-java-apps-644194c23872#.odwmsbnbh) technology for building container-less runnable Java applications. With Bootique you can create REST services, webapps, jobs, DB migration tasks, etc. and run them as if they were simple commands. No JavaEE container required! Among other things Bootique is an ideal platform for Java [microservices](http://martinfowler.com/articles/microservices.html), as it allows you to create a fully functional app with minimal setup.

## Quick Links:

* [WebSite](http://bootique.io)
* [Getting Started](http://bootique.io/docs/0/getting-started/index.html)
* [Docs](http://bootique.io/docs/0/bootique-docs/index.html)


## Standard Modules

Below is a growing list of "standard" Bootique modules. With standard modules you can write apps of different kinds: REST services, job containers, DB migrations, etc. If you don't see a module that you need, keep in mind that [writing your own modules](http://bootique.io/docs/0/bootique-docs/index.html#programming-modules) is easy. So you can either integrate some favorite technology of yours or wrap your own code in a custom module.

* [Bootique Cayenne](https://github.com/nhl/bootique-cayenne)
* [Bootique Curator](https://github.com/nhl/bootique-curator) - Zookeeper client.
* [Bootique JDBC](https://github.com/nhl/bootique-jdbc)
* [Bootique Jersey](https://github.com/nhl/bootique-jersey)
* [Bootique Jersey Client](https://github.com/nhl/bootique-jersey-client)
* [Bootique Jetty](https://github.com/nhl/bootique-jetty)
* [Bootique Job](https://github.com/nhl/bootique-job)
* [Bootique LinkMove](https://github.com/nhl/bootique-linkmove)
* [Bootique LinkRest](https://github.com/nhl/bootique-linkrest)
* [Bootique Liquibase](https://github.com/nhl/bootique-liquibase)
* [Bootique Logback](https://github.com/nhl/bootique-logback)
* [Bootique Metrics](https://github.com/nhl/bootique-metrics)
* [Bootique MVC](https://github.com/nhl/bootique-mvc)
* [Bootique Tapestry](https://github.com/nhl/bootique-tapestry)

And a BOM that declares them all with compatible versions:

* [Bootique BOM](https://github.com/nhl/bootique-bom)

## Support

You have two options:
* [Open an issue](https://github.com/nhl/bootique/issues) on GitHub with a label of "help wanted" or "question" (or "bug" if you think you found a bug).
* Post your question on the [Bootique forum](https://groups.google.com/forum/#!forum/bootique-user).
