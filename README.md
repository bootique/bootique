[![Build Status](https://travis-ci.org/nhl/bootique.svg)](https://travis-ci.org/nhl/bootique)

Bootique is a minimally opinionated technology for building single-jar runnable Java applications of any kind. With Bootique you can create and run REST services, webapps, jobs, etc. as if they were simple commands. No JEE container required!

Bootique was inspired by two similar products - [Dropwizard](http://www.dropwizard.io) and [SpringBoot](http://projects.spring.io/spring-boot/), however its focus is different. Bootique favors modularity and clean pluggable architecture.

Bootique is built on top of [Google Guice](https://github.com/google/guice) DI container, that is the core of its modularity mechanism.

## Getting Started

Declare Bootique Maven repository in your pom.xml (unless you have your own repo proxy, in which case add this repo to the proxy):

```XML
<repositories>
    <repository>
        <id>bq-repo</id>
        <name>Bootique Repo</name>
        <url>http://maven.objectstyle.org/nexus/content/repositories/bootique</url>
    </repository>
</repositories>
```
_TODO: eventually we'll start publishing Bootique to Central, so the step above will not be needed._

Add Bootique dependency:

```XML
<dependency>
	<groupId>com.nhl.bootique</groupId>
	<artifactId>bootique</artifactId>
	<version>0.7</version>
</dependency>
<!-- 
  Below add any number of Bootique extensions. We'll be building a JAX-RS webservice here, 
  so the list may look like this:
-->
<dependency>
	<groupId>com.nhl.bootique.jetty</groupId>
	<artifactId>bootique-jetty</artifactId>
	<version>0.5</version>
</dependency>
<dependency>
	<groupId>com.nhl.bootique.jersey</groupId>
	<artifactId>bootique-jersey</artifactId>
	<version>0.6</version>
</dependency>
<dependency>
	<groupId>com.nhl.bootique.logback</groupId>
	<artifactId>bootique-logback</artifactId>
	<version>0.5</version>
</dependency>
```
Write a main class that configures app's own DI Module, builds extensions Modules and starts Bootique app. In the example below we are setting up a JAX-RS application and the application class also serves as a JAX-RS resource:

```Java
import java.util.Arrays;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyBundle;
import com.nhl.bootique.jetty.JettyBundle;
import com.nhl.bootique.jopt.Args;
import com.nhl.bootique.logback.LogbackBundle;

@Path("/hello")
@Produces("text/plain")
@Singleton
public class LRApplication {

	public static void main(String[] args) throws Exception {

		Module jetty = JettyBundle.create().context("/").port(3333).module();
		Module jersey = JerseyBundle.create().packageRoot(LRApplication.class).module();
		Module logback = LogbackBundle.logbackModule();

		Bootique.app(args).modules(jetty, jersey, logback).run();
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

Now you can run it in your IDE, you will see a list of supported options printed. Add ```--server``` option and run it again. Your webservice should start. Now you can open [http://127.0.0.1:3333/hello/](http://127.0.0.1:3333/hello/) in the browser and see it return a piece of text with program arguments.

## YAML Config

## YAML Config Property Overrides
