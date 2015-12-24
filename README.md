[![Build Status](https://travis-ci.org/nhl/bootique.svg)](https://travis-ci.org/nhl/bootique)

Bootique is a minimally opinionated technology for building single-jar runnable Java applications. With Bootique you can create and run REST services, webapps, jobs, DB migration tasks, etc. as if they were simple commands. No JEE container required! Among other things Bootique is an ideal platform for Java [microservices](http://martinfowler.com/articles/microservices.html), as it allows you to create a fully functional app with minimal setup.

Bootique was inspired by two similar products - [Dropwizard](http://www.dropwizard.io) and [SpringBoot](http://projects.spring.io/spring-boot/), however its focus is different. Bootique favors modularity and clean pluggable architecture.

Bootique is built on top of [Google Guice](https://github.com/google/guice) DI container, that is the core of its modularity mechanism.

## Prerequisites

* Java 8 or newer
* Maven 3.+. Though of course the examples below can be ported to Gradle or any other java build system.
* _A few Bootique extensions (namely, [Cayenne](https://github.com/nhl/bootique-cayenne) and [LinkMove](https://github.com/nhl/bootique-linkmove)) require custom Maven repository declarations in the POM. Follow the links above for details. Hopefully this requirement will go away soon._

## Getting Started

Add Bootique dependency:

```XML
<dependency>
	<groupId>com.nhl.bootique</groupId>
	<artifactId>bootique</artifactId>
	<version>0.10</version>
</dependency>
<!-- 
  Below add any number of Bootique extensions. We'll be building a JAX-RS webservice here, 
  so the list may look like this:
-->
<dependency>
	<groupId>com.nhl.bootique.jersey</groupId>
	<artifactId>bootique-jersey</artifactId>
	<version>0.8</version>
</dependency>
<dependency>
	<groupId>com.nhl.bootique.jersey</groupId>
	<artifactId>bootique-jetty</artifactId>
	<version>0.8</version>
</dependency>
<dependency>
	<groupId>com.nhl.bootique.logback</groupId>
	<artifactId>bootique-logback</artifactId>
	<version>0.7</version>
</dependency>
```
Write a main class that rins Bootique. In this example we are creating a JAX-RS application and the main application class also serves as a JAX-RS resource:

```Java
package com.example;

import java.util.Arrays;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jopt.Args;

@Path("/hello")
@Produces("text/plain")
@Singleton
public class Application {

	public static void main(String[] args) throws Exception {
	
		// Application itself is a JAX RS resource, so register it with Jersey
		Module jersey = new JerseyModule().packageRoot(Application.class);
		
		// include our configured instance of JerseyModule, 
		// all other modules will be included automatically from dependencies
		Bootique.app(args).modules(jersey).autoLoadModules().run();
	}
	
	@Args
	@Inject
	private String[] args;

	@GET
	public String get() {
		String allArgs = Arrays.asList(args).stream().collect(joining(" "));
		return "Hello! The app args were: " + allArgs;
	}
}
```

Now you can run it in your IDE, you will see a list of supported options printed. Add ```--server``` option and run it again. Your webservice should start. Now you can open [http://127.0.0.1:8080/hello/](http://127.0.0.1:8080/hello/) in the browser and see it return a piece of text with program arguments.

## Runnable Jar

It was easy to run the app from the IDE, as we have access to the ```main``` method. It is equally easy to build a runnable .jar file that can be used to run your app anywhere. The easiest way to achieve that is to set ```bootique-parent``` as a parent of your app  pom.xml *(This is not strictly required. If you don't want to inherit from Bootique POM, simply copy its ```maven-shade-plugin``` configuration in your own POM)* :

```XML
<parent>
	<groupId>com.nhl.bootique.parent</groupId>
	<artifactId>bootique-parent</artifactId>
	<version>0.9</version>
</parent>
```
Other needed POM additions:
```XML
<properties>
	<main.class>com.example.Application</main.class>
</properties>
...
<build>
	<plugins>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-shade-plugin</artifactId>
		</plugin>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-jar-plugin</artifactId>
		</plugin>
	</plugins>
</build>
```
Now you can run a Maven build and execute the jar:
```sh
mvn package
java -jar target/myapp-1.0.jar --server
```

## Bootique Modules

Bootique is just a small DI-based launcher that doesn't do much by itself. Its power comes from being a *command-line plugin environment* that can run modules. There's a growing list of "standard" modules provided by Bootique development team. And you can easily write your own. An module is a piece of Java code (usually distributed as a single jar file) that contains some code and a [Guice Module class](https://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/Module.html) that binds module-specific services. Module services can rely on services declared in the [Bootique core module](https://github.com/nhl/bootique/blob/master/src/main/java/com/nhl/bootique/BQCoreModule.java). 

As you see we are using the word "module" either to refer to a Guice Module class, or to a whole a code module. The meaning should be clear from the context.

Most modules can be autoloaded via ```Bootique.autoLoadModules()``` as long as they are included in your aplication dependencies. Autloading is built on the Java [ServiceLoader mechanism](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html). To support auto loading of your own modules, first implement ```com.nhl.bootique.BQModuleProvider``` interface to create a Module instance for your own extension, and then include a file ```META-INF/services/com.nhl.bootique.BQModuleProvider``` with the only line containing the name of your BQModuleProvider implementor. E.g.:

```
com.foo.MyOwnModuleProvider
```
During autoloading modules are configured using YAML config (as described below). Some modules additionally allow configuration in the code (e.g. see [JerseyModule](https://github.com/nhl/bootique-jersey/blob/master/src/main/java/com/nhl/bootique/jersey/JerseyModule.java) shown in the "Getting Started" example above).

## Standard Modules

Below is a growing list of "standard" Bootique modules. With standard modules you can write apps of different kinds: REST services, job containers, DB migrations, etc.:

* [Bootique Cayenne](https://github.com/nhl/bootique-cayenne)
* [Bootique JDBC](https://github.com/nhl/bootique-jdbc)
* [Bootique Jersey](https://github.com/nhl/bootique-jersey)
* [Bootique Jetty](https://github.com/nhl/bootique-jetty)
* [Bootique Job](https://github.com/nhl/bootique-job)
* [Bootique Liquibase](https://github.com/nhl/bootique-liquibase)
* [Bootique Logback](https://github.com/nhl/bootique-logback)
* [Bootique Zookeeper](https://github.com/nhl/bootique-zookeeper)

## YAML Config

## YAML Config Property Overrides

## Tracing Bootique Startup

To see what modules are loaded and to trace other events that happen on startup, run your jar with ```-Dbq.trace``` option. E.g.:

```
java -Dbq.trace -jar target/myapp-1.0.jar --server 
```
You may see an output like this:
```
Skipping module 'JerseyModule' provided by 'JerseyModuleProvider' (already provided by 'Bootique$$396873410')...
Adding module 'BQCoreModule' provided by 'Bootique$$Lambda$3/1199823423'...
Adding module 'JerseyModule' provided by 'Bootique$$Lambda$2/396873410'...
Adding module 'JettyModule' provided by 'JettyModuleProvider'...
Adding module 'LogbackModule' provided by 'LogbackModuleProvider'...
```
