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
[![build test deploy](https://github.com/bootique/bootique/workflows/build%20test%20deploy/badge.svg)](https://github.com/bootique/bootique/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique/bootique.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique/bootique)

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

* [WebSite](https://bootique.io)
* [Getting Started](https://bootique.io/docs/2.x/getting-started/)
* [Docs](https://bootique.io/docs/) - documentation collection for Bootique core and all standard 
  modules.

## Support

You have two options:
* [Open an issue](https://github.com/bootique/bootique/issues) on GitHub with a label of "help wanted" or "question" 
  (or "bug" if you think you found a bug).
* Post a question on the [Bootique forum](https://groups.google.com/forum/#!forum/bootique-user).

## TL;DR

For the impatient, here is how to get started with Bootique:

* Declare the official module collection:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>3.0-RC1</version>
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
        <groupId>io.bootique.jersey</groupId>
        <artifactId>bootique-jersey</artifactId>
    </dependency>
    <dependency>
        <groupId>io.bootique.logback</groupId>
        <artifactId>bootique-logback</artifactId>
    </dependency>
</dependencies>
```
* Write your app:
```java
package com.foo;

import io.bootique.Bootique;

public class Application {
    public static void main(String[] args) {
        Bootique
            .app(args)
            .autoLoadModules()
            .exec()
            .exit();
    }
}
```
It has ```main()``` method, so you can run it! 

*For a more detailed tutorial proceed to [this link](https://bootique.io/docs/2.x/getting-started/).*

## Upgrading

See the "maven-central" badge above for the current production version of ```bootique-bom```. 
When upgrading, don't forget to check [upgrade notes](https://github.com/bootique/bootique/blob/master/UPGRADE.md) 
specific to your version.
