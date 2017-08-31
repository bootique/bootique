# UPGRADE INSTRUCTIONS

## 0.24

* [bootique #180](https://github.com/bootique/bootique/issues/180): The ability to expliictly declare a BQ_* var was removed. 
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
