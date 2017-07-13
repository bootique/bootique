---
title: "Getting Started with Bootique"
metaKeywords: "Bootique Framework Documentation version 0"
metaDescription: "Bootique: A Minimally Opinionated Framework for Runnable Java Apps - Documentation version 0"
---
## Chapter 1. Hello World in Bootique

The goal of this chapter is to write a simple REST app using Bootique. Let's start with a new Java Maven project created in your favorite IDE. Your `pom.xml` in addition to the required project information tags will need to declare a few BOM ("Bill of Material") imports in the `<dependencyManagement/>` section:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.bootique.bom</groupId>
      <artifactId>bootique-bom</artifactId>
      <version>0.23</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

This will allow  `<dependencies/>` section that will follow shortly to include various Bootique modules and not worry about their individual versions. So let's create this section and import a few modules:

```xml
<dependencies>
  <dependency>
    <groupId>io.bootique.jersey</groupId>
    <artifactId>bootique-jersey</artifactId>
    <scope>compile</scope>
  </dependency>
  <dependency>
    <groupId>io.bootique.logback</groupId>
    <artifactId>bootique-logback</artifactId>
    <scope>compile</scope>
  </dependency>
</dependencies>
```

As you see we want a `bootique-jersey` and `bootique-logback` modules in our app. Those may depend on other modules, but we don't have to think about it. Those dependencies will be included by Maven automatically. Now let's create the main Java class that will run the app:

```java
package com.foo;

import io.bootique.Bootique;

public class Application {

    public static void main(String[] args) {
        Bootique.app(args).autoLoadModules().run();
    }
}
```
There's only one line of meaningful code inside the `main()` method, but this is already a working Bootique app. Meaning it is runnable and can do a few things. So let's try running this class from your IDE. You will see the output like this on the IDE console:

```
NAME
      com.foo.App

OPTIONS
      -c yaml_location, --config=yaml_location
           Specifies YAML config location, which can be a file path or a URL.

      -h, --help
           Prints this message.

      -H, --help-config
           Prints information about application modules and their configuration
           options.

      -s, --server
           Starts Jetty server.
```

So the app printed its help message telling us which commands and options it understands.  `--server` option looks promising, but before we use it, let's actually write a REST endpoint that will answer to our requests. We'll use standard Java [JAX-RS API](https://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) for that:

```java
package com.foo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class HelloResource {

    @GET
    public String hello() {
        return "Hello, world!";
    }
}
```

Note that we could have placed this code straight in the Main class. Which makes for an effective demo (an app that can fit in one class), but not for a particularly clean design. So keeping the resource in its own class. Now let's amend the `Main` class to tell Bootique where to find the resource:

```java
package com.foo;

import com.google.inject.Module;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;

public class Application {

    public static void main(String[] args) {

        Module module = binder -> 
                  JerseyModule.extend(binder).addResource(HelloResource.class);

        Bootique.app(args).module(module).autoLoadModules().run();
    }
}
```

Here we created our own module that "contributes" resource configuration to the JerseyModule. Now let's try to run the app with the changes. Add `--server` to the command run parameters before doing it. Now when the app is started, you will see different output:

```text
INFO main o.e.jetty.util.log: Logging initialized @1328ms
INFO main i.b.j.s.ServerFactory: Adding listener io.bootique.jetty.servlet.DefaultServletEnvironment
INFO main i.b.j.s.ServletFactory: Adding servlet 'jersey' mapped to /*
INFO main i.b.j.s.ServerLifecycleLogger: Starting jetty...
INFO main o.e.j.server.Server: jetty-9.3.6.v20151106
INFO main o.e.j.s.h.ContextHandler: Started o.e.j.s.ServletContextHandler@27dc79f7{/,null,AVAILABLE}
INFO main o.e.j.s.ServerConnector: Started ServerConnector@3a45c42a{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
INFO main o.e.j.server.Server: Started @2005ms
INFO main i.b.j.s.ServerLifecycleLogger: Started Jetty in 584 ms. Base URL: http://127.0.0.1:8080/
```

Notice that the app did not terminate immediately, and is waiting for user requests. Now let's try opening the URL [http://localhost:8080/](http://localhost:8080/) in the browser. We should see 'Hello, world!' as request output. We just built a working REST app that does not require deployment to a web container, and generally wasn't that hard to write. The takeway here is this:

* You start the app via `Bootique` class, that gives you a runnable "shell" of your future app in one line of code.
* Declaring modules in the app dependencies and using `Bootique.autoLoadModules()` gives the app the ability to respond to commands from those modules (in our example `--server` command coming from implicit bootique-jetty module started an embedded web server ).
* You can contribute your own code to modules to build an app with desired behavior.

Next we'll talk about configuration...

## Chapter 2. Configuration

You can optionally pass a configuration to almost any Bootique app. This is done with a `--config` parameter. An argument to `--config` is either a path to a configuration file or a URL of a service that serves such configuration remotely (imagine an app starting on a cloud that downloads its configuration from a central server). The format of the file is YAML (though, just like everything in Bootique, this can be customized). Let's create a config file that changes Jetty listen port and the app context path. To do this create a file in the app run directory, with an arbitrary name, e.g. `myconfig.yml` with the following contents:

```yaml
jetty:
  context: /hello
  connector:
    port: 10001
```

Now restart the app with the new set of parameters: `--server --config=myconfig.yml`. After the restart the app would no longer respond at [http://localhost:8080/](http://localhost:8080/), instead you will need to use a new URL: [http://localhost:10001/hello](http://localhost:10001/hello). This is just a taste of what can be done with configuration. Your app can just as easily obtain its own specific configuration in a form of an app-specific object, as described elsewhere in the docs.

## Chapter 3. Injection

We've mentioned that Bootique is built on Google Guice dependency injection (DI) container. We'll talk more about injection elsewhere. Here we'll provide a simple example. Our simple app already has a number of objects and services working behind the scenes that can be injected. One of them is command-line arguments that were provded to the app on startup. Let's extend our resource to include those arguments in the output:

```java
package com.foo;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.inject.Inject;
import io.bootique.annotation.Args;

@Path("/")
public class HelloResource {

    @Inject
    @Args
    private String[] args;

    @GET
    public String hello() {
        String allArgs = Arrays.asList(args).stream().collect(joining(" "));
        return "Hello, world! The app was started with the following arguments: " + allArgs;
    }
}
```

As you see, we declared a variable of type `String[]` and annotated it with `@Inject` and `@Args`. `@Inject` (must be a `com.google.inject.Inject`, not `javax.inject.Inject`) ensures that the value is initialized via injection, and `@Args` tells Bootique which one of possibly many String[] instances from the DI container we are expecting here.

Now you can restart the app and refresh [http://localhost:10001/hello](http://localhost:10001/hello) in the browser. The new output will be "Hello, world! The app was started with the following arguments: `--server --config=myconfig.yml`".

Next let's discuss how to build and run the app outside the IDE...

## Chapter 4. Packaging

Till now we've been running our app from IDE (which also happened to be much easier then running typical container-aware apps). Now let's package our app as a runnable "fat" jar to be able to run it from command line (e.g. in deployment environment). Assembling "fat" jar requires a bit of configuration of the Maven `maven-shade-plugin`. To simplify it, you can set a parent of your `pom.xml` to be a standard Bootique parent:

```xml
<parent>
    <groupId>io.bootique.parent</groupId>
    <artifactId>bootique-parent</artifactId>
    <version>0.12</version>
</parent>
```

Other required `pom.xml` additions:

```xml
<properties>
    <main.class>com.foo.Application</main.class>
</properties>
<!--...-->
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

Once this is setup you can build and run the app:

```text
mvn clean package

# Using myapp-1.0.jar as an example; the actual jar name depends on your POM settings
java -jar target/myapp-1.0.jar --server --config=myconfig.yml
```

The result should be the same as running from the IDE and the app should be still accessible at [http://localhost:10001/hello](http://localhost:10001/hello). Now your jar can be deployed in any environment that has Java 8.

This concludes our simple tutorial. Now you can explore our [documentation](http://bootique.io/docs/) to read more about Bootique core and individual modules.
