# UPGRADE INSTRUCTIONS

## 0.21

* [bootique-liquibase #4](https://github.com/bootique/bootique-liquibase/issues/4):  'liquibase.changeLog' YAML config 
property is deprecated, but still supported. The new alternative is 'changeLogs' collection. When migrating to 
'changeLogs', ensure that you rewrite the paths in Bootique YAML files as well as in Liquibase XML/YAML change logs to
follow Bootique ResourceFactory approach. Specifically, if the resource is expected to be on classpath, it requires
"classpath:" prefix. Otherwise it will be treated as a file path.

* [bootique #105](https://github.com/bootique/bootique/issues/105): The new default style for multi-word command classes
that are spelled in camel-case will result in the name parts separated with dash, while previously
we'd use no separators. E.g. ```MySpecialCommand.java``` will result in command name being "--myspecial" in 0.20, 
and "--my-special" in 0.21. This affects you if you've written custom command classes with multi-word names. 
You can manually override public command names in metadata to return to the old style (see 
[CommandWithMetadata](https://github.com/bootique/bootique/blob/master/bootique/src/main/java/io/bootique/command/CommandWithMetadata.java)) or embrace the new naming scheme. 

* [bootique-jdbc #6](https://github.com/bootique/bootique-jdbc/issues/6): bootique-jdbc uses Tomcat DataSource that has 
 a few dozens of config properties. As we migrated JDBC configuration from a map to a 
 [specific class](https://github.com/bootique/bootique-jdbc/blob/master/bootique-jdbc/src/main/java/io/bootique/jdbc/TomcatDataSourceFactory.java),
 we dropped support for certain noop properties there were supported but ignored in the previous configuration. If you 
 get configuration errors, review your configs and remove those properties. Additionally the following properties
 are treated differently than before:
 
  - ```connectionProperties``` property is not supported (as it is unclear why it may be useful)
  - ```jmxEnabled``` default is changed from true to false.
  - ```object_name``` is renamed to ```jmxObjectName```.
  

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
