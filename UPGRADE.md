<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->


# UPGRADE INSTRUCTIONS

_Upgrade instructions to earlier versions, up to 3.0, are available [here](UPGRADE-3.0.md)_ 

## 4.0-M2

### [bootique-jetty #129](https://github.com/bootique/bootique-jetty/issues/129): After Jetty 12 upgrade we stopped 
collecting the `ThreadPool.QueuedRequests` metric. While Jetty still provides "queueSize" property, the number it returns 
is not the same as the number of requests waiting in the queue, as it combines in the same count both requests and some 
internal "jobs". So we can no longer report this accurately. Instead, this metric will always report "0" (and will be 
removed later in 5.x). This also affects the corresponding health check (which will always succeed now). Our 
recommendation is to stop watching this metric and watch `ThreadPool.Utlization` instead.

### [bootique-jetty #129](https://github.com/bootique/bootique-jetty/issues/129): After Jetty 12 upgrade, `RequestMDCItem` 
callback methods changed to take `org.eclipse.jetty.server.Request` as a parameter instead of `ServletContext` and 
`ServletRequest`, as now it is invoked outside the scope of the "servlet" objects. This change will only affect your 
code if you implemented custom "MDC items", but otherwise should be transparent.

### [bootique-job #135](https://github.com/bootique/bootique-job/issues/135): If you managed canceling and restarting
job triggers via `ScheduledJob`, you will get compilation errors and will need to switch to a cleaner new API
described in this task (such as `scheduler.cancelTriggers(..)`, etc.)

### [bootique-shiro #48](https://github.com/bootique/bootique-shiro/issues/28): _Only applies if you are upgrading from
`4.0-M1`._ JWKS and audience properties are now configured in the upstream `bootique-shiro-jwt` module. You need to 
rename `shirowebjwt` top-level configuration key to `shirojwt`.

### [bootique-jersey #100](https://github.com/bootique/bootique-jersey/issues/100): WADL application descriptor is now
disabled by default to avoid potential security issues. In the unlikely event that WADL is needed, it can be re-enabled
like this:

Add JAXB implementation dependency:
```xml
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>4.0.5</version>
</dependency>
```
Explicitly "undisable" WADL in Jersey:
```java
JerseyModule.extend(b).setProperty("jersey.config.server.wadl.disableWadl", false);
```

### [bootique-cayenne #119](https://github.com/bootique/bootique-cayenne/issues/119): Cayenne module configuration 
structure has changed. If you relied on the implicit default location of the project file (`cayenne-project.xml` on
the classpath), you will need to add it explicitly:
```java
CayenneModule.extend(b).addLocation("classpath:cayenne-project.xml");
```
If you added Cayenne projects in Java code, you will need to heed the deprecation warnings and switch to `addLocation(..)`:

```java
// old...
// CayenneModule.extend(b).addProject("org/example/cayenne-project.xml");
// new...
CayenneModule.extend(b).addLocation("classpath:org/example/cayenne-project.xml");
```

If you used "cayenne.maps" configuration, you will need to provide an explicit "cayenne-project.xml" location for all
those DataMaps. And you will still have a way to link them with Bootique DataSources:

```yaml
cayenne:
   locations:
      - classpath:org/example/cayenne-project.xml
   mapDatasources:
      m1: ds1
      m2: ds2
```

### [bootique-jersey #101](https://github.com/bootique/bootique-jersey/issues/101): **`@Singleton` annotations started to 
matter!!** When upgrading from the deprecated
resource registration `JerseyModule.extend(b).addResource(..)` to `addApiResource(..)`, remember that the old methods
effectively treated your API resources as singletons, regardless of whether classes (or their Bootique "provides"
methods) were annotated with `@Singleton` or not. `addApiResource(..)` will respect the resource scope, and suddenly you 
may end up with lots of per-request endpoints, taking a performance hit of their constant re-creation. Moreover, some
resources are occasionally stateful (e.g., in a test, you might have a request counter within a resource). Those will
break when they become per-request. So the safest upgrade approach would be to explicitly annotate those endpoints with `@Singleton`:

```java
@Singleton
@Path("p")
public class MyApi {}

JerseyModule.extend(binder).addApiResource(MyApi.class);
```

```java
@Path("p")
public class MyOtherApi {}

public class MyModule implements BQModule {
    
    @Override
    public void configure(Binder b) {
        JerseyModule.extend(binder).addApiResource(MyOtherApi.class);
    }

    @Provides
    @Singleton
    MyOtherApi provideMe() {
        return new MyOtherApi();    
    }
}
```

## 4.0-M1

### Finalizing a switch to Jakarta: This affects the core and the majority of modules. "javax" based deprecated modules
were removed, and the names of the remaining modules containing `-jakarta` where shortened. E.g. `bootique-jetty` 
was removed, and `bootique-jetty-jakarta` was renamed back to `bootique-jetty`. What this means from the upgrade 
perspective is this:
* If you are still using the deprecated "javax" modules, you'll need to revisit the source code and change the import 
packages to `jakarta.xxx`. Your build files (`pom.xml` or gradle files) for the most part will stay unchanged.
* If you previously switched to 3.0 `jakarta` modules, the soyrce code will stay the same, but you will need to 
rename your Bootique dependencies in the build files, removing `-jakarta` from their names.
* Pay special attention to replacing `javax.inject` annotations with `jakarta.inject`. The old `javax.inject` stuff was
still working in 3.0, even in the context of Jakarta modules, but now it will be simply ignored.
* If you are using Bootique testing extensions, note that deprecated JUnit 4 integrations are removed, so we'll suggest
 switching to JUnit 5 and much more powerful JUnit 5 testing extensions.

### [bootique-jcache #15](https://github.com/bootique/bootique-jcache/issues/15): JCache bootstrap procedure was rewritten
to be more "native" to Bootique. It will no longer use the static provider caches from `javax.cache.Caching`, and will
manage `CacheManager` in the scope of a single Bootique runtime. This affects the user-visible behavior in the following
ways:
* `javax.cache.spi.CachingProvider` system property will be ignored when bootstrapping the provider. This should not 
  be an issue with most popular providers, as the implicit provider loading from `META-INF/services/javax.cache.spi.CachingProvider`
  is fully supported and most popular third-party caches use it. But if you still need to specify the provider explicitly,
  use `jcache.provider` Bootique config.
* In an unlikely event that multiple Bootique runtimes (in tests or otherwise) relied on a shared cache state crossing
  runtime boundaries, this will no longer work, as the state is not shared (not in memory at least). Such code will need
  to be refactored.
