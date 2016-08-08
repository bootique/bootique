# UPGRADE INSTRUCTIONS

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
  files sitting somewhere in your source. Make sure you rename these files to ```META-INF/services/io.bootique.BQModuleProvider```.

* If you've written any polymorphic config extensions, you will have ```META-INF/services/com.nhl.bootique.config.PolymorphicConfiguration``` 
  files sitting somewhere in your source. Make sure you rename these files to ```META-INF/services/io.bootique.config.PolymorphicConfiguration```.


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
