[![Build Status](https://travis-ci.org/nhl/bootique.svg)](https://travis-ci.org/nhl/bootique)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.nhl.bootique/bootique/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.nhl.bootique/bootique/)

Bootique is a [minimally opinionated](https://medium.com/@andrus_a/bootique-a-minimally-opinionated-platform-for-modern-java-apps-644194c23872#.odwmsbnbh) 
java launcher and integration technology. It is intended for building container-less runnable Java applications. 
With Bootique you can create REST services, webapps, jobs, DB migration tasks, etc. and run them as if they were 
simple commands. No JavaEE container required! Among other things Bootique is an ideal platform for 
Java [microservices](http://martinfowler.com/articles/microservices.html), as it allows you to create a fully-functional
app with minimal setup.

Each Bootique app is a collection of modules interacting with each other via dependency injection. This GitHub project 
provides Bootique core. Bootique team also develops a number of important modules. A full list is available 
[here](http://bootique.io/docs/).

## Quick Links

* [WebSite](http://bootique.io)
* [Getting Started](http://bootique.io/docs/0/getting-started/)
* [Docs](http://bootique.io/docs/) - documentation collection for Bootique core and all standard 
  modules.

## Support

You have two options:
* [Open an issue](https://github.com/nhl/bootique/issues) on GitHub with a label of "help wanted" or "question" 
  (or "bug" if you think you found a bug).
* Post a question on the [Bootique forum](https://groups.google.com/forum/#!forum/bootique-user).

## TL;DR

For the impatient, here is how to get started with Bootique:

* Import two official module collections:
```xml
<dependencyManagement>
	<dependencies>
		<dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>0.18</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency> 
		<dependency>
			<groupId>com.nhl.bootique.bom</groupId>
			<artifactId>bootique-bom</artifactId>
			<version>0.18</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```
* Include the modules that you need:
```xml
<dependencies>
	<dependency>
		<groupId>com.nhl.bootique.jersey</groupId>
		<artifactId>bootique-jersey</artifactId>
		<scope>compile</scope>
	</dependency>
	<dependency>
		<groupId>com.nhl.bootique.logback</groupId>
		<artifactId>bootique-logback</artifactId>
		<scope>compile</scope>
	</dependency>
</dependencies>
```
* Write your app:
```java
package com.foo;

import com.nhl.bootique.Bootique;

public class Application {
	public static void main(String[] args) {
		Bootique.app(args).autoLoadModules().run();
	}
}
```
It has ```main()``` method, so you can run it! 

*For a more detailed tutorial proceed to [this link](http://bootique.io/docs/0/getting-started/).*

## Upgrading

See the "maven-central" badge above for the current production version of the 2 ```bootique-bom``` collection modules. 
When upgrading, don't forget to check [upgrade notes](https://github.com/nhl/bootique-bom/blob/master/UPGRADE.md) 
specific to your version.
