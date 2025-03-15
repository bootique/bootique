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

## 3.0-RC1

* [bootique #349](https://github.com/bootique/bootique/issues/349): `jakarta.inject` annotations
  (`jakarta.inject.Inject`, etc.) and DI-related interfaces (`jakarta.inject.Provider`) are now a preferred flavor in
  Bootique dependency injection instead of `javax.inject`. Unlike other modules (e.g. Jetty), that ship `javax`
  and `jakarta` flavors as two separate modules that can be included in the app independently, Bootique core supports
  (for now) both `jakarta.inject` and `javax.inject` in the same codebase. Meaning you can keep using 
  `javax.inject.Inject`, `javax.inject.Provider`, etc. until at least 4.0. But this will result in deprecation warnings 
  in IDE and runtime logs. We suggest that you heed those warnings and switch to the Jakarta whenever you can.

## 3.0-M6

* [bootique-job #124](https://github.com/bootique/bootique-job/issues/124): As a part of alignment of the Job outcome 
object with Bootique Command API, the `Job.run()` now returns `JobOutcome` instead of `JobResult`. This part is 
backwards-compatible (as `JobResult` is deprecated but preserved and inherits from `JobOutcome`). However, there `enum JobOutcome`, 
got renamed (without deprecation) to `enum JobStatus`, so if you used `JobResult.getOutcome()` you may get a compilation
error. Here is how to fix it:

```java
// JobOutcome o = job.run().getOutcome();
JobStatus o = job.run().getStatus();
```

## 3.0-M3

* [bootique #344](https://github.com/bootique/bootique/issues/344): `bootiqie-di` was merged into Bootique core, and is no longer 
shipped as a standalone module. This allows us to integrate it more tightly with Bootique concepts, such as module autoloading, 
app configuration, etc., improving user experience. If you have a special case that requires an Injector, but not the rest of 
Bootique, you can still do it. Just include `bootique` module as your dependency, and keep using `DIBootstrap` like you did before to create an `Injector`. Though it will result in a few extra dependencies, like Jackson (that can probably be excluded explicitly).

* [bootique #340](https://github.com/bootique/bootique/issues/340): API-based module dependency tracking is no longer supported.
While `BQModuleProvider` (deprecated per the following note) still has a method called `dependencies()`, its output is ignored 
by the runtime. If you relied on implicit loading of transitive dependenices, you have two choices - switch to autoloading, or manually 
list all upstream modules your application depends on when assembling Bootique. 

* [bootique #344](https://github.com/bootique/bootique/issues/344): `BQModuleProvider` API was deprecated. It is still supported
until Bootique 4.0, but it much easier to load your modules directly: Modules now can be autoloaded (like providers were,
but from `META-INF/services/io.bootique.BQModule` declarations), and can define their own metadata (via a new `BQModule.crate()` 
method that can be optionally implemented). E.g. consider the following provider:
```java
public class MyModuleProvider implements BQModuleProvider {

  @Override
  public BQModule module() {
    return new MyModule();
  }

  @Override
  public Map<String, Type> configs() {
    return Collections.singletonMap("my", MyFactory.class);
  }

  @Override
  public BQModuleMetadata.Builder moduleBuilder() {
    return BQModuleProvider.super
            .moduleBuilder()
            .description("Provides a very important function");
  }
}
```
It can be removed, and the following code added to the Module:
```java
public class MyModule implements BQModule {
  ...
  @Override
  public ModuleCrate crate() {
    return ModuleCrate.of(this)
            .description("Provides a very important function")
            .config("my", MyFactory.class)
            .build();
  }
}
```
So we end up with one class instead of two, and with a much more compact syntax. For autoloading, 
create `META-INF/services/io.bootique.BQModule` file with the name of the module class, and remove
`META-INF/services/io.bootique.BQModuleProvider`, as we no longer have a provider.

* [bootique-jdbc #132](https://github.com/bootique/bootique-jdbc/issues/132): Tomcat DataSource support got deprecated,
with the advice to switch to Hikari DataSource. This requires a couple of steps: (1) replace Tomcat modules with
`bootique-jdbc-hikiricp` or `bootique-jdbc-hikiricp-instrumented`, (2) check your app configurations for DataSources,
as Hikari uses different property names. Namely, `url` should be renamed to `jdbcUrl`, but all the other pool parameters
also need to be renamed. Run your app with `-H` to see the available config properties, and refer to HikariCP docs for
in-depth explanation.

## 3.0.M2

* [bootique-linkmove #54](https://github.com/bootique/bootique-linkmove/issues/54): LinkMove 3.0 simplified its earlier
  confusing approach of handling connector factories, and now allows multiple factory implementations for a given 
  connector type (e.g. `StreamConnector`). This allowed us to streamline `bootique-linkmove3` integration, making 
  `Set<IConnectorFactory>` directly injectable without an intermediate `IConnectorFactoryFactory`. After upgrading most 
  existing BQ LinkMove projects should continue to work. Still a couple of things to pay attention to if you are using 
  LinkMove 3:

  * If you had `linkmove.connectorFactories` in your configuration, it will now be ignored, and you can safely remove it.
  * If you had an explicit configuration of a connector factory of type "uri", you will need to migrate it to a 
    jerseyclient-based configuration. For this you will need to add `bootique-linkmove3-rest` as a dependency and 
    change your configuration following the example below:
```yaml
# Before
linkmove:
  extractorsDir: "classpath:extractors"
  connectorFactories:
    - type: "uri" # <- if you have this type of connector, you need to change it
      connectors:
        name1: "http://example.org/f1.json"
        name2: "http://example.org/f2.json"
```

```yaml
# After
linkmove:
  extractorsDir: "classpath:extractors"
  
jerseyclient:
  targets:
    name1:
      url: "http://example.org/f1.json"
    name2:
      url: "http://example.org/f2.json"
```

* [bootique-jetty #117](https://github.com/bootique/bootique-jetty/issues/117): JettyTester's ResponseMatcher will no 
longer allow to read the response content twice, nor will it allow reading the content after "assertContent" was called.
So after the upgrade you start getting "Response data is already read" exceptions, you will need to review your test code and make sure 
you call "getContentXyz()" or "assertContent(..)" only once for a given response.

* [bootique-mvc #25](https://github.com/bootique/bootique-mvc/issues/25): A unified templates / sub-templates resolving
mechanism was implemented with a strict same-origin policy. A backwards-incompatible part of the new algorithm is 
handling of the absolute URLs of the "root" templates. If you specify a template name with a starting "/" in your view, 
it will be interpreted as relative to the app "templateBase" and the view class package name will not be prepended to
the template path. To fix it, you will need to remove the leading slash.

* [bootique-jersey #80](https://github.com/bootique/bootique-jersey/issues/80): If you are using Jersey client
instrumentation, note that a metric name reported in the healthcheck was changed from `bq.JerseyClient.Threshold.Requests` 
to `bq.JerseyClient.Requests.PerMin`, and default `warning/critical` thresholds changed from `3/15` to `60/120`. Also, 
healthcheck thresholds configuration was restructured and requires an update:

```yaml
# The old structure looked like this:
#jerseyclientinstrumented:
#  timeRequestsThresholds:
#    warning: 1
#    critical: 5

# The new should look like this:
jerseyclient:
  health:
    requestsPerMin:
      warning: 1
      critical: 5
```

## 3.0.M1

* [bootique #317](https://github.com/bootique/bootique/issues/317): Minimal Java version required by Bootique is now
  Java 11. If you are still on Java 8, you will need to upgrade your environment.

* [bootique-tapestry #24](https://github.com/bootique/bootique-tapestry/issues/24): Since Tapestry 5.4 does not support
  Java 11, `bootique-tapestry` module (based on T5.4) was removed. You will need to replace it with `bootique-tapestry55`
  or newer.

* [bootique-rabbitmq #20](https://github.com/bootique/bootique-rabbitmq/issues/20) , 
  [bootique-rabbitmq #21](https://github.com/bootique/bootique-rabbitmq/issues/21): `rabbitmq.exchanges` and `rabbitmq.queues`
  are no longer directly mapped to the physical exchanges and queues. Rather those are configuration templates used to 
  create exchanges/queues by the endpoints API or the user code. This resulted in a few incompatible changes in
  the configuration:
  * For sub endpoints, `rabbitmq.sub.[endpoint].exchange` and `rabbitmq.sub.[endpoint].queue` properties are removed.
  They are replaced with `rabbitmq.sub.[endpoint].exchangeConfig`, `rabbitmq.sub.[endpoint].exchangeName`,
  `rabbitmq.sub.[endpoint].queueConfig`, `rabbitmq.sub.[endpoint].queueName`, where the `*Config` property is an optional 
  reference to the corresponding exchange/queue "template" configuration. And the optional `*Name` is the RabbitMQ 
  object name. Names can be further changed using subscription builder API, without changing configuration.
  * Similarly, for pub endpoints, `rabbitmq.pub.[endpoint].exchange` property is removed.
  It is replaced with `rabbitmq.pub.[endpoint].exchangeConfig`, `rabbitmq.pub.[endpoint].exchangeName`, where 
  "exchangeConfig" property is an optional reference to the corresponding exchange "template" configuration. And the 
  optional "exchangeName" is the RabbitMQ object name. The name can be further changed using publisher builder API, 
  without changing configuration.
  * `RmqChannelFactory` class was removed. Instead you should use the `RmqEndpoints` API. If it is too high-level,
  and you would like to work with channels and topologies directly, you can also use injectable `RmqChannelManager` / 
  `RmqTopologyManager` combo:

```java
// old code, no longer available
// @Inject
// RmqChannelFactory channelFactory;
// Channel c = channelFactory.newChannel("c1").ensureExchange("e1").open();

// new code
@Inject
RmqChannelManager channelManager;
@Inject
RmqTopologyManager topologyManager;

Channel c = channelManager.createChannel("c1");
new RmqTopologyBuilder()
    // in the new API config name is purely symbolic
    // in the old it matched the exchange name
    .ensureExchange("e1", topologyManager.getExchangeConfig("e1"))
    .build(c);
```

* [bootique-kafka #30](https://github.com/bootique/bootique-kafka/issues/30):  If you were using `KafkaConsumerRunner`,
  you will have to switch to `consume(KafkaConsumerCallback,Duration)`. The callback is invoked on a batch of data after 
  each poll.

* [bootique-job #92](https://github.com/bootique/bootique-job/issues/92):  The logger name in the logs for 
  "job started" / "job finished" / "job exception" events was changed from `JobLogListener` to 
  either `JobLogger`

* [bootique-job #93](https://github.com/bootique/bootique-job/issues/93): If you define your own LockHandler 
  implementation, do not override JobModule, and do not redefine LockHandler injection point. Instead, call the extender 
  like this: `JobModule.extend(binder).setLockHandler(MyLockHandler.class)`

* [bootique-job #95](https://github.com/bootique/bootique-job/issues/95):  Previously exceptions in JobListeners were
  logged and ignored. From now on an exception in the start or finish method of the listener will fail the job. So if
  your jobs suddenly start failing, there is a chance that there was an unhandled listener exception that is no longer
  ignored.

* [bootique-job #107](https://github.com/bootique/bootique-job/issues/107), 
  [bootique-job #110](https://github.com/bootique/bootique-job/issues/110):  To standardize `bootique-job` module layout,
  classes that the user may reference directly were moved to `io.bootique.job` package from various subpackages. To resolve
  the resulting compilation errors, update the import package. Here are the classes that you are most likely to 
  encounter in the app code:
  * `JobResult` was moved from `io.bootique.job.runnable` to `io.bootique.job` (a deprecated version of JobResult is still present in the old location, but still consider switching the import)
  * `JobOutcome` was moved from `io.bootique.job.runnable` to `io.bootique.job`
  * `Scheduler` was moved from `io.bootique.job.scheduler` to `io.bootique.job`
  * `JobModule` was moved from `io.bootique.job.runtime` to `io.bootique.job`

* [bootique-cayenne #97](https://github.com/bootique/bootique-agrest/issues/97): Cayenne 4.0 is no longer supported.
  If you import `io.bootique.cayenne:bootique-cayenne` in your project (as well as `io.bootique.cayenne:bootique-jcache`,
  `io.bootique.cayenne:bootique-cayenne-test` and `io.bootique.cayenne:bootique-cayenne-junit5`) they will not be 
  available. You will need to change the import to `io.bootique.cayenne:bootique-cayenne41` or
  `io.bootique.cayenne:bootique-cayenne42` (depending on which version you are upgrading to). Same goes for `-jcache`,
  `-test` and `-junit5` modules.

* [bootique-cayenne #100](https://github.com/bootique/bootique-agrest/issues/100): `CayenneTester` 
  was made much more robust by getting rid of eager initialization of Cayenne stack. But any existing code that
  implicitly relied on eager initialization would now be expected to fail. One example is when `CayenneTester` is used for DB
  schema creation. Schema creation action would only run upon `ServerRuntime` instantiation. But you may have a 
  `DbTester` (that knows nothing about `CayenneTester`) accessing the DB prior to that. As a result all its operations 
  would fail until ServerRuntime is resolved. The best solution would be to refactor the code and avoid such conditions,
  but alternatively you can call `CayenneyTester.getRuntime()` manually before using the `DbTester`.

* [bootique-agrest #62](https://github.com/bootique/bootique-agrest/issues/62), 
  [bootique-agrest #68](https://github.com/bootique/bootique-agrest/issues/68): Agrest modules are reorganized around
  versions of Agrest instead of versions of Cayenne. We now support Agrest 4.x and 5.x (both are currently on Cayenne 4.2).
  So there's no more `bootique-agrest`, `bootique-agrest-cayenne41`, `bootique-agrest-cayenne42`. Instead, there are 
  modules like `bootique-agrest4`, `bootique-agrest5` and their Jakarta doubles. You project needs to be switched to 
  Cayenne 4.2 from 4.1 or 4.0 to work with either of them.

* [bootique-linkmove #49](https://github.com/bootique/bootique-linkmove/issues/49), 
  [bootique-linkmove #50](https://github.com/bootique/bootique-linkmove/issues/50): Only Cayenne 4.2 is supported in
  combination with LinkMove. And it can work either with LinkMove 2 or 3. You will need to upgrade your Cayenne
  model to 4.2 if you are still on 4.1 or 4.0. Next, regardless of the version of Cayenne you were on (even if 
  you were already on 4.2), you will need to change the coordinates of your dependencies:
  * Either of `bootique-linkmove`,`bootique-linkmove-cayenne41`,`bootique-linkmove-cayenne42` should be replaced with 
   `bootique-linkmove2` or `bootique-linkmove3`
  * Either of `bootique-linkmove-json`,`bootique-linkmove-json-cayenne41`,`bootique-linkmove-json-cayenne42` should be replaced with
    `bootique-linkmove2-json` or `bootique-linkmove3-json`
  * Either of `bootique-linkmove-rest`,`bootique-linkmove-rest-cayenne41`,`bootique-linkmove-rest-cayenne42` should be replaced with
    `bootique-linkmove2-rest` or `bootique-linkmove3-rest`


## 2.0 (final)

* [bootique-cayenne #97](https://github.com/bootique/bootique-cayenne/issues/97): `maps` configuration option is changed
  from **list** to **map** for Cayenne 4.1 and 4.2 to align it with Cayenne 4.0 behavior that was implemented earlier
  per [bootique-cayenne #68](https://github.com/bootique/bootique-cayenne/issues/68). You need to upgrade your yml 
  configs accordingly.

## 2.0.RC1

* [bootique-jetty #109](https://github.com/bootique/bootique-jetty/issues/109): To fix `JettyTester` initialization 
  sequence we had to make a subtle change in its lifecycle. Now you can only call `JettyTester.getUrl()` or 
  `JettyTester.getPort()` when the test app is already started with `--server` command. Previously these methods 
  worked on test runtimes even before they were started. The most typical affected scenario is when a test
  class caches its client "target" object:

```java
static final JettyTester jetty = JettyTester.create();
...
static final WebTarget target = jetty.getTarget();
```
  Instead you should call `jetty.getTarget()` on the spot inside the unit test.


## 2.0.B1

* [bootique #300](https://github.com/bootique/bootique/issues/300): The new API allows to register configuration-loading
  extensions like this:
  
```java
BQCoreModule.extend(b).addConfigLoader(MyLoader.class);
```
This obsoletes a couple of existing mechanisms. If you relied on `ConfigurationSource` object or on
`BQCoreModule.extend(b).addPostConfig("..")` (introduced in 2.0.M1), they will no longer be available and should be
replaced with custom `JsonConfigurationLoader`.

* [bootique-metrics #41](https://github.com/bootique/bootique-metrics/issues/41): A few configuration keys were changed
for `heartbeat`. Rename the following config keys in YAML or properties: 

* `heartbeat.initialDelayMs` to `heartbeat.initialDelay`
* `heartbeat.fixedDelayMs` to `heartbeat.fixedDelay`
* `heartbeat.healthCheckTimeoutMs` to `heartbeat.healthCheckTimeout`

While you are at it, you can change the values to human-readable "durations":

```yaml
# was:
# heartbeat:
#   healthCheckTimeoutMs: 20000
# became:
heartbeat:
 healthCheckTimeout: 20sec
```

* [bootique-jdbc #106](https://github.com/bootique/bootique-jdbc/issues/106): Delete with condition API got improved.
If you are using delete with condition anywhere in tests (`table.delete().and(..)` or `table.delete().or(..)`), you
will get a compilation error. Change those calls to `table.delete().where(..)` to fix it.

* [bootique-jdbc #107](https://github.com/bootique/bootique-jdbc/issues/107): Table API related to SELECT queries
got reworked. If you receive compilation errors in tests either because `Table.selectColumns` signature is different, 
or `Table.get*` or `Table.select*` methods are missing, consider replacing your code with either the new select API 
(`Table.selectColumns(..)`) or a matcher (`Table.get*` are an indicator that you likely need a matcher to compare 
values in DB instead of selecting them in Java).

* [bootique-rabbitmq #8](https://github.com/bootique/bootique-rabbitmq/issues/8): There is a substantial refactoring of 
  the `ChannelFactory` API. For one thing it was renamed to `io.bootique.rabbitmq.client.channel.RmqChannelFactory`. 
  Second, if you want to create an RMQ Channel with a specific topology, you would use `RmqChannelFactory.newChannel(connectionName)`, and then use the available builder methods to describe the desired topology. You will need to
  update your code accordingly.

## 2.0.M1 

* [bootique #269](https://github.com/bootique/bootique/issues/269) Upgrade from Guice DI to "bootique-di". That's the BIG one! 
The core of Bootique - its dependency injection engine - was switched over from Google Guice to a faster, lighter 
["bootique-di"](https://github.com/bootique/bootique-di). The two DI implementations are conceptiually rather close, so 
there is a straightforward migration path. More details are provided in the 
[DI Migration Guide](https://bootique.io/docs/latest/migrate-from-guice/). 

* [bootique #271](https://github.com/bootique/bootique/issues/271): Bootique is now integrated with JUnit 5. A few notes:
 
  * Use `@RegisterExtension` to annotate `BQTestFactory`. For class-scoped test factory use the new `BQTestClassFactory`,
  also annotated with `@RegisterExtension`.
  * JUnit 4 compatibility. You still have the option to use JUnit 4, and can take your time to 
[migrate to 5](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4). Note that JUnit 4 support
is now done via JUnit 5 "Vintage" engine and JUnit itself is a _compile_ dependency of "bootique-test" (it used to be 
"_provided_"). So if you import "bootique-test", you can remove an explicit JUnit dependency, and can mix and match
4 and 5 style tests in your code. 
  * `BQDaemonTestFactory` is not available under 5, as it is fairly useless and doesn't bring any advantages over 
  `BQTestFactory`.
  
* [bootique #288](https://github.com/bootique/bootique/issues/288): `Bootique.module(BQModuleProvider)` and 
`BQTestRuntimeBuilder.module(BQModuleProvider)` were renamed to `moduleProvider` to avoid arguments ambiguity. If you 
get compilation errors for these methods, replace them with `moduleProvider(BQModuleProvider)`.

* [bootique-cayenne #68](https://github.com/bootique/bootique-cayenne/issues/68): `maps` configuration option is changed 
from **list** to **map**. You need to upgrade your yml configs accordingly.

* [bootique-jersey #47](https://github.com/bootique/bootique-jersey/issues/47): An internal injection point 
(a `Set<Package>` listing Java packages that contain Jersey components) is now using an extra annotation to avoid 
polluting common DI namespace. In an unlikely event that your code explicitly injects a `Set<Package>` to get custom 
Jersey components, you will need to annotate the injection point with `@JerseyResource`.

* [bootique-agrest #42](https://github.com/bootique/bootique-agrest/issues/42): Previously deprecated `bootique-linkrest`
integration with the legacy LinkRest framework is removed. If you are still using it, you should be able to switch to 
the new well-supported incarnation of this framework called Agrest. On Bootique side, simply replace the import
of `bootique-linkrest` with `bootique-agrest`, and on LinkRest side follow the 
[upgrade instructions](https://github.com/agrestio/agrest/blob/master/UPGRADE-NOTES.md#upgrading-to-30) back from 
Agrest version 3.0.

* [bootique-job #82](https://github.com/bootique/bootique-job/issues/82): Previously deprecated trigger 
millisecond-based configs properties got removed. Please check your configurations and replace them with duration-based 
analogs (durations are string that contain units, e.g. "5ms", "6sec", etc.) :
                        
  * `scheduler.triggers.fixedRateMs` should become `scheduler.triggers.fixedRate`
  * `scheduler.triggers.fixedDelayMs` should become `scheduler.triggers.fixedDelay`
  * `scheduler.triggers.initialDelayMs` should become `scheduler.triggers.initialDelay`

To update your config, change it from something like:
```yaml
scheduler:
  triggers:
    - job: j1
      trigger: t1
      initialDelayMs: 10
      fixedDelayMs: 10000
```
to something like this:
```yaml
scheduler:
  triggers:
    - job: j1
      trigger: t1
      initialDelay: 10ms
      fixedDelay: 10sec
```

* [bootique-kafka #28](https://github.com/bootique/bootique-kafka/issues/28): Support for the legacy Kafka client v 0.8
is removed. A newer client should be used. See [https://github.com/bootique/bootique-kafka] for details.

* [bootique-jetty #95](https://github.com/bootique/bootique-jetty/issues/95): Default value for `maxThreads` property was 
lowered from 1024 to 200. If you relied on this default value you should now set it explicitly in the app config.

* [bootique-jersey #48](https://github.com/bootique/bootique-jersey/issues/48): `bootique-jersey-client` is no longer 
a "top-level" project and was merged to `bootique-jersey`. There's no code changes, but the module coordinates have changed.
Instead of `io.bootique.jersey.client:bootique-jersey-client` you will need to import `io.bootique.jersey:bootique-jersey-client`.

* [bootique-rabbitmq #1](https://github.com/bootique/bootique-rabbitmq/issues/1): `bootique-rabitmq-client` artifact 
"group" changed from `io.bootique.rabbitmq.client` to `io.bootique.rabbitmq`. Adjust your build scripts accordingly.

* [bootique-rabbitmq #2](https://github.com/bootique/bootique-rabbitmq/issues/2): There was a deep configuration system
refactoring:

  * `ChannelFactory` was moved to `io.bootique.rabbitmq.client` package. Adjust your imports accordingly.
  * `ConnectionFactory` was renamed to `ChannelManager` and is no longer injectable. You will rarely need to access it
  directly, but if you do, you can retrieve it from `ChannelFactory`
  * `ChannelFactory` methods no longer require a `Connection` parameter. Pass the connection *name* instead. The old
  methods are still around, but are deprecated.

* [bootique-swagger #18](https://github.com/bootique/bootique-swagger/issues/18): The default URL of the Swagger UI 
has changed from "/swagger" to "/swagger-ui". You'll need to either start using the new URL, or change it via configuration
property `swaggerui.<label>.uiPath`.

* [bootique-swagger #21](https://github.com/bootique/bootique-swagger/issues/21): To support the same Swagger UI servlet 
serving multiple models in one app ("multitenancy"), "swaggerui" configuration is now nested, consisting of zero or
more named configurations. To update your config, change it from something like:
```yaml
swaggerui:
  specUrl: "https://example.org/spec1.yml"
```
to something like this:
```yaml
swaggerui:
  default: # "default" is an arbitrary name. You can use your own
     specUrl: "https://example.org/spec1.yml"
```

* [bootique-swagger #24](https://github.com/bootique/bootique-swagger/issues/24): `bootique-swagger` (Swagger backend)
was upgraded to support OpenAPI spec 3.0 (instead of the legacy Swagger 2.0). If you were using Swagger annotations,
you will need to migrate from "io.swagger:swagger-annotations" to "io.swagger.core.v3:swagger-annotations".

* [bootique-curator #17](https://github.com/bootique/bootique-curator/issues/17): With Curator dependency upgrade, 
`bootique-curator` now supports Zookeeper 3.5 or newer.

## 1.1

* [bootique #266](https://github.com/bootique/bootique/issues/266): To improve Java 11 compatibility, Bootique core
was upgraded to the latest Guice v 4.2.2. This is more of an FYI. No action is needed from the users, unless you 
are manually managing Guice versions in your app.

* [bootique-agrest #37](https://github.com/bootique/bootique-agrest/issues/37): While `bootique-agrest` didn't change, 
the underlying Agrest framework introduced a few breaking changes between 3.1 and 3.2, that hopefully affect only a small subset of users:

  * Response with overlapping relationship / attribute "includes" is `include` order dependent [#406](https://github.com/agrestio/agrest/issues/406)
  * @QueryParameter types changed to Strings and Integers [#408](https://github.com/agrestio/agrest/issues/408) If you are not directly injecting `Include`, `Exclude` and friends (injecting `UriInfo` instead), are you are not affected.

See [Agrest upgrade instructions](https://github.com/agrestio/agrest/blob/master/UPGRADE-NOTES.md#upgrading-to-32) for more details. 

* [bootique-linkmove #37](https://github.com/bootique/bootique-linkmove/issues/37): LinkMove library was upgraded to 
v 2.7. While `bootique-linkmove` didn't change, the underlying LinkMove framework introduced a few breaking changes 
between 2.6 and 2.7. Namely switching of internal ETL data representation to DFLib DataFrame. If your get any 
compilation errors as a result, see [LinkMove upgrade instructions](https://github.com/nhl/link-move/blob/master/UPGRADE-NOTES.md)
for details of the 2.7 changes.

## 1.0

Fully compatible with 1.0.RC1

## 1.0.RC1

* [bootique-agrest #35](https://github.com/bootique/bootique-agrest/issues/35): Group of `bootique-linkrest` artifact
changed from `io.bootique.linkrest` to `io.bootique.agrest`.

* [bootique #252](https://github.com/bootique/bootique/issues/252): Method `defaultValue()` in OptionMetadata builder 
changed to `valueOptionalWithDefault()`

* [bootique #251](https://github.com/bootique/bootique/issues/251): Method `addConfigOnOption(String optionName, String configResourceId)`
in `BQCoreModuleExtender` renamed to `mapConfigResource`

* [bootique #249](https://github.com/bootique/bootique/issues/249): Confusing methods 
`addOption(String configPath, String defaultValue, String name)` and `addOption(String configPath, String name)` 
in `BQCoreModuleExtender` are replaced with new one `mapConfigPath(String optionName, String configPath)`
 that has much clearer signature. **Note** that option that will be mapped to config path should be added separately. 

* [bootique #188](https://github.com/bootique/bootique/issues/188): Guice was upgraded from 4.0 to
4.2.0 for better support of the latest versions of Java. This changes the version of Guava lib to
`23.6-android` and that Guava brings with it a whole set of other libraries (see the screenshot
[here](https://groups.google.com/forum/#!topic/bootique-user/elLjQXaK-40). If your code depends
on Guava elsewhere, be aware of this change. Also (and this is very unfortunate) this upgrade
increased the dependency footprint by ~500K.

* [bootique #214](https://github.com/bootique/bootique/issues/214): All APIs previously deprecated are
removed from Bootique core. Please recompile your code and fix any failures (refer to 0.25 JavaDocs for
suggested API replacements).

The most notable change though, that you will not notice just by recompiling, is that Bootique now
completely ignores `BQ_` environment variables that used to set config properties. Please inspect
your app launch environment (be it IDE or server/Docker/cloud) to see if you still rely on such variables.
Switch to explicitly declared variables instead:

```java
// FWIW, you can use your old BQ_ var name here if you feel like it.
// Just need to bind it explicitly.
BQCoreModule.extend(bidner).declareVar("a.b.c", "MY_VAR");
```

* [bootique-jetty #75](https://github.com/bootique/bootique-jetty/issues/75): As a part of deprecated
API removal `bootique-jetty-test` module was removed. It is enough to use
`bootique-test` as described in the [0.25 blog](https://blog.bootique.io/the-state-of-bootique-early-2018-part-1-ed6806d9c99a)
("Test API Improvements" section).

* [bootique-undertow #13](https://github.com/bootique/bootique-undertow/issues/13): As a part of deprecated
API removal `bootique-undertow-test` module was removed. It is enough to use
`bootique-test` as described in the [0.25 blog](https://blog.bootique.io/the-state-of-bootique-early-2018-part-1-ed6806d9c99a)
("Test API Improvements" section).

* [bootique-jetty #77](https://github.com/bootique/bootique-jetty/issues/77): Jetty health checked thresholds structure has
changed as shown in the following diff:

```yaml
jetty:
    health:
-    queuedRequestsThreshold: "2 3"
-    poolUtilizationThreshold: "0.6 0.9"
+    queuedRequestsThresholds:
+      warning: 2
+      critical: 3
+    poolUtilizationThresholds:
+      warning: 60%
+      critical: 90%
```
* [bootique-jdbc #81](https://github.com/bootique/bootique-jdbc/issues/81): Hikari health check threashold structure has changed
as shown in the following diff. Now you can specify warning and critical ranges and use human-readable durations:

```yaml
jdbc:
  mydb:
    health:
-      connectivityCheckTimeout: 250
-      expected99thPercentile: 10
+      connectivity:
+        critical: 250ms
+      connection99Percent:
+        warning: 10ms
+        critical: 500ms
```

* [bootique-jdbc #82](https://github.com/bootique/bootique-jdbc/issues/82): `bootique-jdbc-instrumented` module was removed.
There are healthchecks specific to Tomcat and Hikari pools, so there's no need to have a generic set of healthchecks that interferes
with them. You will need to remove references to `bootique-jdbc-instrumented` from your build scripts and replace them with `bootique-jdbc-tomcat-instrumented` (that actually contains healtchecks ported from the removed module) or `bootique-jdbc-hikaricp-instrumented` (that has its own set of health checks).

* [bootique-job #64](https://github.com/bootique/bootique-job/pull/64): If you used Zookeeper for job locking, configuration is done differently
now. You will need to remove `scheduler.clusteredLocks: true` from YAML, and instead add a dependency on `io.bootique.job:bootique-job-zookeeper`
(or the new `io.bootique.job:bootique-job-consul`). Either will enable lock clustering, but with its own mechanism.

* [bootique-job #66](https://github.com/bootique/bootique-job/pull/66): Trigger configurations switched from numeric milliseconds to
"durations". While the configuration is backwards-compatible, we'd encourage you to review it and replace config keys "fixedDelayMs",
"fixedRateMs", "initialDelayMs" with "fixedDelay", "fixedRate", "initialDelay" respectively. While you are at it, you may switch to
more user-friendly forms, such as "3s" or "5min".

* [bootique-kafka #19](https://github.com/bootique/bootique-kafka/issues/19): `bootique-kafka-client` repository was renamed to `bootique-kafka`.
If you are using `bootique-kafka-client`, you will need change the Maven "groupId" to `io.bootique.kafka` from `io.bootique.kafka.client`.

* [bootique-kafka #21](https://github.com/bootique/bootique-kafka/issues/21): "kafkaclient.producer.lingerMs" property got renamed to
"kafkaclient.producer.linger" and was changed to a Duration type. Please change all references to `kafkaclient.producer.lingerMs`
in your config to be `kafkaclient.producer.linger`. Note that once you do it, you can use any supported duration format e.g. "1s".
Also note that the default value for this property was changed from 1 to 0 to match the Kafka default.

* [bootique-kafka #23](https://github.com/bootique/bootique-kafka/issues/23): Per this task, a new user-friendly Consumer API was
created. Breaking changes:

  * Consumers can no longer be obtained via `KafkaClientFactory`. Instead inject `KafkaConsumerFactory` and use its Consumer builder methods.
  * Producers can no longer be obtained via `KafkaClientFactory`. Instead inject `KafkaproducerFactory` and use its Producer builder methods.
  * `kafkaclient.consumer.autoCommitIntervalMs` config is renamed to
`kafkaclient.consumer.autoCommitInterval` and is now a duration (so you can use readable values like "1ms"). Same goes
for `kafkaclient.consumer.sessionTimeoutMs` that got renamed to `kafkaclient.consumer.sessionTimeout`.

* [bootique-metrics #30](https://github.com/bootique/bootique-metrics/issues/30): There was a massive renaming of module metrics to
follow a single naming convention. Follow the link to [bootique-metrics #30](https://github.com/bootique/bootique-metrics/issues/30)
to see the old vs new names across all affected modules.

* [bootique-metrics #31](https://github.com/bootique/bootique-metrics/issues/31): Similarly to metrics, health checks got similarly renamed to
follow the same naming convention. Follow the link to [bootique-metrics #31](https://github.com/bootique/bootique-metrics/issues/31)
to see the old vs new names across all affected modules.

* [bootique-agrest #34](https://github.com/bootique/bootique-agrest/issues/34) : LinkRest was changed to Agrest 
and bootique-agrest. Bootique-linkrest now deprecated. Please use bootique-agrest dependency.


## 0.25

* [bootique-jdbc #48](https://github.com/bootique/bootique-jdbc/issues/48): This affects all users of `bootique-jdbc`,
including indirect users, such as those relying on `bootique-cayenne`, `bootique-linkrest`, `bootique-linkmove`, 
`bootique-jooq`, `bootique-liquibase`, etc. Due to `bootique-jdbc` module becoming "abstract", if you have a JDBC app, on startup you will 
see an error like this:

```
No concrete 'bootique-jdbc' implementations found. You will need to add one 
(such as 'bootique-jdbc-tomcat', etc.) as an application dependency.
``` 
To fix the problem, you will need to do exactly as suggested, i.e. add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>io.bootique.jdbc</groupId>
    <artifactId>bootique-jdbc-tomcat</artifactId>
    <scope>compile</scope>
</dependency>
```
The original `bootique-jdbc` is now a transitive dependency of the new `bootique-jdbc-tomcat`, so you may remove 
explicit `bootique-jdbc` import (or leave it, as no harm is being done by keeping it around). If you are using 
`bootique-jdbc-instrumented` (i.e. JDBC with metrics), import a different dependency:

```xml
<dependency>
    <groupId>io.bootique.jdbc</groupId>
    <artifactId>bootique-jdbc-tomcat-instrumented</artifactId>
    <scope>compile</scope>
</dependency>
```

* [bootique-jdbc #66](https://github.com/bootique/bootique-jdbc/issues/66): DataSource connection health checks were 
renamed to uniquely identify their origin and purpose. E.g. a health check that may have previously been called 
`mydb` after the DataSource name, will now be called `bq.jdbc.mydb.canConnect`. If any of your code or the monitoring
system depended on the certain name format, you will need to update it accordingly.


* [bootique-metrics #20](https://github.com/bootique/bootique-metrics/issues/20): Health check module was separated
from the metric module. If you used healthcheck API in your code and are getting compilation errors now, you will need 
to add an extra dependency:

```xml
<dependency>
	<groupId>io.bootique.metrics</groupId>
	<artifactId>bootique-metrics-healthchecks</artifactId>
	<scope>compile</scope>
</dependency>
```
Additionally if you called `MetricsModule.extend(binder)`, replace it with `HealthCheckModule.extend(binder)`.

* [bootique-jetty #69](https://github.com/bootique/bootique-jetty/issues/69): There was a slight change in health check 
servlet output format. In case of errors the format now includes specific status (CRITICAL, WARNING, UNKNOWN). E.g.:

```yaml
// old format
! h3: ERROR - I am not healthy

// new format
! h3: WARNING - I am not healthy
! h3: CRITICAL - I am not healthy
```

* [bootique-jetty #70](https://github.com/bootique/bootique-jetty/issues/70): With introduction of explicit per-connector 
`acceptorThreads` and `connectorThreads` configuration properties, the defaults used by the server for acceptors and 
selectors have changed from 0.5 acceptor and 1 selector thread(s) per CPU core to  0.125 acceptor and 0.5 selector
thread(s) per CPU core. In the unlikely event that you need the old values back, you can configure them using the
new properties. E.g.:

```yaml
jetty:
  connectors:
    - acceptorThreads: 4
      selectorThreads: 8
```

* [bootique-jetty #71](https://github.com/bootique/bootique-jetty/issues/71): We decided to clarify the meaning of the
Jetty thread pool metrics. The old "utilization" metric was not very useful as it compared used threads against the 
current thread pool size. But since the pool can grow/shrink at any moment, it didn't tell anything about the state of the
app. So its meaning is now replaced to be the same as the old "utilization-max" (i.e. a ratio of used threads to the 
max possible threads in the pool). "utilization-max" was deprecated. 

If you used "utilization" metric for anything in your monitoring system, etc., make sure to adjust for the new 
meaning.

* [bootique-jersey-client #29](https://github.com/bootique/bootique-jersey-client/issues/29): `followRedirects` default value
has changed from "false" to "true". If you implicitly relied on `jerseyclient.followRedirects` default to be "false", you will need to reconfigure your app to set it to false explicitly:
```yaml
jerseyclient:
   followRedirects: false
```

## 0.24

* [bootique #180](https://github.com/bootique/bootique/issues/180): The ability to explicitly declare a BQ_* var was removed. 
In other words this API is not longer available: 
                  
  ```java
  // this used to declare a variable BQ_PROP_XYZ
  BQCoreModule.extend(binder).declareVar("prop.xyz")
  ``` 
  You have two choices for upgrading your code: either (1) simply remove this line of code (`BQ_PROP_XYZ` will keep working, 
  just won't show in help), or (2 - better) assign this variable an app-specific name. E.g. 
  `BQCoreModule.extend(binder).declareVar("prop.xyz", "MY_VAR")`.

* [bootique-jdbc #39](https://github.com/bootique/bootique-jdbc/issues/39): If you had unit tests that are using `Table.insertFromCsv(..)` and the `.csv` files contain timestamps, replace the space between date and time portions with "T" symbol. The new format is proper ISO-8601. 

## 0.23

* [bootique #141](https://github.com/bootique/bootique/issues/141): If you've ever implemented your own "main" method 
instead of relying on the one from `io.bootique.Bootique`, you may want to replace the call to the deprecated `Bootique.run()` 
with `Bootique.exec().exit()`. Notice how the new API allows to insert custom code after Bootique finish, but before the 
app exit.

* [bootique #142](https://github.com/bootique/bootique/issues/142): This issue introduces API-breaking changes to the
integration testing API. Instead of `BQTestRuntime` and `BQDaemonTestRuntime`, test factories now produce simply 
`BQRuntime`, that helps to focus on the object being tested instead of test wrappers. Upgrade instructions:

  1. `BQTestRuntime` and `BQDaemonTestRuntime` are gone, so replace references to them with just `BQRuntime`. 
  2. A common call to `testRuntime.getRuntime().getInstance(..)` should now be shortened to `testRuntime.getInstance(..)`.
  3. If you need to get an outcome of a background task execution, instead of `daemonTestRuntime.getOutcome()` you need 
  to call `daemonTestFactory.getOutcome(runtime)` ("daemonTestFactory" is a `BQDaemonTestFactory` or subclasses like
   `JettyTestFactory`, that are the `@Rule`'s in your test). 
  4. Use factory API (the same approach as in the previous point) if you need to manually start/stop the daemon runtime:
  `daemonTestFactory.start(runtime)`, `daemonTestFactory.stop(runtime)`.
  5. If you need to capture STDOUT or STDERR of a specific BQRuntime run, use the new `TestIO` class that can capture 
  this data. It additionally allows to suppress trace logging, making your test console much less verbose than before. 
  E.g.:
  
	```java
	TestIO io = TestIO.noTrace();
	testFactory.app("-x")
		.bootLogger(io.getBootLogger())
		.createRuntime()
		.run();

	assertEquals("--out--", io.getStdout());
	assertEquals("--err--", io.getStderr());
	```
	Other modules that have been affected by this change are 
	[bootique-jetty-test](https://github.com/bootique/bootique-jetty/issues/59),
	[bootique-undertow-test](https://github.com/bootique/bootique-undertow/issues/3), 
	[bootique-jdbc-test](https://github.com/bootique/bootique-jdbc/issues/32),
	[bootique-cayenne-test](https://github.com/bootique/bootique-cayenne/issues/39).

* [bootique-liquibase #16](https://github.com/bootique/bootique-liquibase/issues/16): Names of Liquibase module 
commands have been changed by adding prefix "lb" to distinguish them from commands of others modules.

    Old options:    
                      
      --changelog-sync
           Mark all changes as executed in the database.

      --changelog-sync-sql
           Writes SQL to mark all changes as executed in the database to STDOUT.

      --clear-check-sums
           Clears all checksums in the current changelog, so they will be
           recalculated next update.

      -u, --update
           Updates DB with available migrations

      -v, --validate
           Checks the changelog for errors.
          
    New options:

      --lb-changelog-sync
           Mark all changes as executed in the database.

      --lb-changelog-sync-sql
           Writes SQL to mark all changes as executed in the database to STDOUT.

      --lb-clear-check-sums
           Clears all checksums in the current changelog, so they will be
           recalculated next update.

      -u, --lb-update
           Updates DB with available migrations

      -v, --lb-validate
           Checks the changelog for errors.

Short names has been preserved for partial backwards compatibility.

## 0.22

* [bootique-cayenne #36](https://github.com/bootique/bootique-cayenne/issues/36): If you used `bootique-cayenne-jcache`, 
note that a big chunk of its functionality is now handled by Cayenne itself. From the upgrade perspective only one thing is affected:
if you ever used custom `InvalidationHandler`, you will need to switch that to `org.apache.cayenne.lifecycle.cache.InvalidationHandler` 
that has a slightly different method signature (due to the need to keep Java 7 compatibility in Cayenne 4.0).


## 0.21

* [bootique #105](https://github.com/bootique/bootique/issues/105): The new default style for multi-word command classes
that are spelled in camel-case will result in the name parts separated with dash, while previously
we'd use no separators. E.g. `MySpecialCommand.java` will result in command name being "--myspecial" in 0.20, 
and "--my-special" in 0.21. This affects you if you've written custom command classes with multi-word names. 
You can manually override public command names in metadata to return to the old style (see 
[CommandWithMetadata](https://github.com/bootique/bootique/blob/master/bootique/src/main/java/io/bootique/command/CommandWithMetadata.java)) or embrace the new naming scheme. 

* [bootique #112](https://github.com/bootique/bootique/issues/112): The implementation of this feature has a few 
(intentional) side-effects: (1) joptsimple library was upgraded to 5.0.6, (2) CLI option abbreviations are no longer 
supported. You may have not known this, but before 0.21, you could use partial option names (e.g. "--he" for help). Not
anymore. Now either a short option or a long option with full name must be used (i.e. "-h" or "--help" in case of the 
help command).

* [bootique #113](https://github.com/bootique/bootique/issues/113): Classes in ```io.bootique.application``` package 
were moved under ```io.bootique.meta.application``` in an effort to centralize Bootique metadata facilities. If you 
referenced them in your code, you will need to fix the imports and recompile. This will primarily affect custom Commands.

* [bootique-jdbc #6](https://github.com/bootique/bootique-jdbc/issues/6): bootique-jdbc uses Tomcat DataSource that has 
 a few dozens of config properties. As we migrated JDBC configuration from a map to a 
 [specific class](https://github.com/bootique/bootique-jdbc/blob/master/bootique-jdbc/src/main/java/io/bootique/jdbc/TomcatDataSourceFactory.java),
 we dropped support for certain noop properties there were supported but ignored in the previous configuration. If you 
 get configuration errors, review your configs and remove those properties. Additionally the following properties
 are treated differently than before:
 
  - ```connectionProperties``` property is not supported (as it is unclear why it may be useful)
  - ```jmxEnabled``` default is changed from true to false.
  - ```object_name``` is renamed to ```jmxObjectName```.
  
* [bootique-liquibase #4](https://github.com/bootique/bootique-liquibase/issues/4):  'liquibase.changeLog' YAML config 
property is deprecated, but still supported. The new alternative is 'changeLogs' collection. When migrating to 
'changeLogs', ensure that you rewrite the paths in Bootique YAML files as well as in Liquibase XML/YAML change logs to
follow Bootique ResourceFactory approach. Specifically, if the resource is expected to be on classpath, it requires
"classpath:" prefix. Otherwise it will be treated as a file path.

* [bootique-logback #26](https://github.com/bootique/bootique-logback/issues/26):  'log.level' and 
'log.loggers.<xyz>.level' must be specified in lowercase. I.e. ```level: debug``` instead of ```level: DEBUG```. 
Uppercase values will now cause an exception.

* [bootique-job #15](https://github.com/bootique/bootique-job/issues/15):  'scheduler.jobPropertiesPrefix' property is no longer supported in configuration and needs to be removed from the config files, etc. Parameters for individual jobs should now be specified under 'params' key (previously they were specified under the job's name). 
```yaml
# prior to 0.21
jobs:
  myjob:
    param1: value1
    
# starting with 0.21
jobs:
  myjob:
    params:
        param1: value1
```

## 0.20

* [bootique #92](https://github.com/bootique/bootique/issues/92): We created a new application metadata package 
at ```io.bootique.application```. The existing metadata objects where moved to this package from different places. 
Specifically ```io.bootique.command.CommandMetadata``` was moved and ```io.bootique.cli.CliOption``` was moved and 
renamed to ```OptionMetadata```.  **This is a breaking change.**  You will need to use module versions aligned with 
0.20 Bootique BOM and fix any imports in your own code (especially in custom Commands).

* [bootique #97](https://github.com/bootique/bootique/issues/97): We simplifed integration test API. It now looks pretty much the same as a regular ```main(String[])``` method. There's no longer a need to use "configurator" lambda to configure test runtime. Instead you'd call Bootique-like methods on the test factory. When upgrading your tests you will need to change calls on ```BQTestFactory``` and ```BQDaemonTestFactory``` from ```newRuntime()``` to ```app(args)```. Terminating builder methods no longer take args (it is passed in the "app" method at the start of the chain). Pay attention to deprecating warnings and use your IDE for available APIs of the test factories.

* [bootique-jetty #52](https://github.com/bootique/bootique-jetty/issues/52): ```JettyTestFactory``` received the same API upgrades as ```BQTestFactory``` and ```BQDaemonTestFactory``` as described above. The upgrade instructions are similar.


## 0.19

The biggest change in 0.19 is combining modules under ```com.nhl.bootique``` and ```io.bootique``` into a single 
namespace - ```io.bootique```. To upgrade, you will need to change module group names in your POM, Java package 
names in imports, and the name of service provider files in your modules. Details are provided below:

* If you used Bootique-provided parent pom, in your ```pom.xml``` change the parent declaration:

```xml
<parent>
	<groupId>io.bootique.parent</groupId>
	<artifactId>bootique-parent</artifactId>
	<version>0.12</version>
</parent>
```
* Change all group names of Maven artifacts in your ```pom.xml``` from ```com.nhl.bootique.*``` to ```io.bootique.*```. 
  Module names are preserved.
   
* Use ```io.bootique``` BOM, remove ```com.nhl.bootique``` BOM:
 
```xml
<dependency>
	<groupId>io.bootique.bom</groupId>
	<artifactId>bootique-bom</artifactId>
	<version>0.19</version>
	<type>pom</type>
	<scope>import</scope>
</dependency>
```

Note that there was a brief period of time (0.18) when required both BOMs to be imported. Not anymore. ```io.bootique```
one is sufficient now.

* Look for Java compilation errors in your IDE, and change ```com.nhl.bootique.*``` import statements to ```io.bootique.*```.

* If you've written any auto-loadable modules, you will have ```META-INF/services/com.nhl.bootique.BQModuleProvider``` 
  files sitting somewhere in your source.Rename these files to ```META-INF/services/io.bootique.BQModuleProvider```.

* Similarly if you've written any polymorphic config extensions, your sources will have ```META-INF/services/com.nhl.bootique.config.PolymorphicConfiguration``` 
  files somewhere. Rename these files to ```META-INF/services/io.bootique.config.PolymorphicConfiguration```.


## 0.18

* [bootique-cayenne #12](https://github.com/nhl/bootique-cayenne/issues/12) : ```CayenneModule.builder()``` is removed.
  Instead of using the builder, use YAML (or another form of) configuration. If you need a project-less Cayenne stack,
  simply do not reference any Cayenne projects (and make sure "cayenne-project.xml" is not on classpath).
  
* [bootique-curator #4](https://github.com/nhl/bootique-curator/issues/4) : ```bootique-zookeeper``` was renamed to
  ```bootique-curator```, so the dependency import and Java package names need to be changed accordingly.


## 0.17

* [bootique #62](https://github.com/nhl/bootique/issues/62) : To fully take advantage of the default main class, make
  sure you upgrade ```com.nhl.bootique.parent:bootique-parent``` to version 0.11.

## 0.12.2

* [bootique-cayenne #9](https://github.com/nhl/bootique-cayenne/issues/9) : CayenneModule ```noConfig``` and
  ```configName``` methods are moved into a builder, and constructor is made private. Now to set cayenne-...xml via
  API use something like this:

```java
CayenneModule.builder()./* configure module */.build();
```
* [bootique-jdbc #4](https://github.com/nhl/bootique-jdbc/issues/4) : Instrumentation is removed from
  ```bootique-jdbc``` and into a separate ```bootique-jdbc-instrumented``` module.If you relied on the
  DataSource metrics, change your import to the new Module.

## 0.12:

* Command API has been redone. If you have modules or apps that implement Commands, 
  you will need to update those. Sample commands can be found in our modules, e.g. 
  [bootique-jobs](https://github.com/nhl/bootique-job/tree/master/src/main/java/com/nhl/bootique/job/command).

  
* @Args annotation was moved to "com.nhl.bootique.annotation" package from "com.nhl.bootique.jopt". 
  Update your imports.

* Now only one command can match command line options. THis was true before, but wasn't enforced.
  So you may see certain command line configs failing with errors, where they appeared to work before
  by pure chance.
  
* BQBinder is deprecated. From within your Module use static methods on BQCoreModule to contribute
  Module-specific commands, properties, CLI options. 
