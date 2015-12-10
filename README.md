Bootique is an unopinionated technology for building single-jar runnable Java applications of any kind. With Bootique you can create and run REST services, webapps, jobs, etc. jars, as if they were simple commands. No JEE container required!

Bootique was inspired by two similar products - [Dropwizard](http://www.dropwizard.io) and [SpringBoot](http://projects.spring.io/spring-boot/), however its focus is different. Bootique does not restrict you in regards to the contents of your app and by default does not bundle any unneeded services. Bootique favors modularity and clean architecture over immediate convenience.

Bootique is built on top of [Google Guice](https://github.com/google/guice) DI container, that is the core of its modularity mechanism.
