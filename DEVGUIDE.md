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

# Bootique Release Guide

Here you can find guidelines for performing a release. The important thing is that Bootique consists of a number of modules dependent between themselves. 
Therefore in the process of release it’s necessary to stick to the order of modules described in the appendix **Release dependency groups**.

Another major point is differentiation of releases: **train** and **hot-fix**. The term train is used to refer to a release of all [Bootique](https://github.com/bootique)
modules, hot-fix - a release of a module on demand of a highly required fix. 

## Prerequisites 

1. Generate a GPG key to sign commits and tags. More info [Generating a new GPG key](https://help.github.com/articles/generating-a-new-gpg-key/), 
[Adding a new GPG key to your GitHub account](https://help.github.com/articles/adding-a-new-gpg-key-to-your-github-account/)

2. Distribute your GPG key. Sent a key to a keyserver (e.g. [pgp.mit.edu/](https://pgp.mit.edu/)) using the command-line. 
See [The GNU Privacy Handbook](https://www.gnupg.org/gph/en/manual/x457.html) for details.

3. To connect and to be authenticated on GitHub server, use SSH key.  
Look at [Generating a new SSH key and adding it to the ssh-agent](https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/).

4. For deploying of maven artifacts to a remote repository [api.bintray.com/maven/bootique/releases](https://api.bintray.com/maven/bootique/releases),  
make sure "bintray-bootique-releases" repository is configured within `distributionManagement` element in the *pom.xml* and the authentication information 
for connecting to the repository - in the *~/.m2/settings.xml*. 
See [this page](http://www.apache.org/dev/publishing-maven-artifacts.html) for details. 
Illustrative *settings.xml*:
```xml
<settings>
    <servers>
        <server>
            <id>bintray-bootique-releases</id>
            <username>your_bintray_username</username>
            <password>your_bintray_API_key</password>
        </server>
    </servers>
</settings>
```

## Train release instructions

* **JCenter Deployment** 
    * Prepare sources
    * Release Maven Artifact
* **Maven Central Synchronization** 

**NOTE:** The steps *prepare sources* and *release maven artifact* are performed cyclically till the last module on the list is released.

## JCenter artifacts deployment

### Prepare sources

1. Build the module to reveal issues of incompatibility.
```bash
mvn clean install
```
2. Fix issues and track them on GitHub if upgrade led to API or serious code changes. 
3. Edit RELEASE-NOTES.md if there is anything to add there.
4. Push changes on GitHub.

### Release Maven Artifact

The artifact is released in 2 stages: preparing the release and performing the release.

* Preparing the release
```bash
mvn release:prepare -Pgpg -Dbootique.version=the release version, e.g. 0.24
```
* Performing the release
```bash
mvn release:perform -Pgpg 
```
Finally, an artifact is deployed to the local repository and to a remote repository configured the pom.xml. 
Check info about a new module release on [bintray.com/bootique/releases](https://bintray.com/bootique/releases).	

## Maven Central Synchronization

When all modules are released and available in [JCenter](https://bintray.com/bintray/jcenter),  the last thing is to publish them into [Maven Central](https://search.maven.org). 
Look through [all modules](https://bintray.com/bootique/releases) and press Maven Central >>Sync. 


## Release dependency groups


Module                  | Dependencies                                      |    
----------------------- | --------------------------------------------------|
bootique                |                                                   |   
bootique-metrics        |                                                   |
bootique-curator        |                                                   |
bootique-jcache         |                                                   |
bootique-logback        |                                                   |
bootique-kafka-client   |                                                   |
bootique-rabbitmq-client|                                                   |
bootique-undertow       |                                                   |
bootique-jdbc           | bootique-metrics                                  |
bootique-jetty          | bootique-metrics                                  |    
bootique-cayenne        | bootique-jdbc, bootique-jcache                    |
bootique-job            | bootique-curator, bootique-metrics                |
bootique-flyway         | bootique-jdbc, bootique-logback                   |    
bootique-liquibase      | bootique-jdbc                                     |        
bootique-jooq           | bootique-jdbc                                     |
bootique-tapestry       | bootique-jetty                                    |
bootique-jersey         | bootique-jetty                                    |
bootique-linkmove       | bootique-jdbc, bootique-cayenne                   |    
bootique-agrest       | bootique-jersey, bootique-cayenne                 |
bootique-jersey-client  | bootique-jersey, bootique-logback                 |
bootique-mvc            | bootique-jetty, bootique-jersey                   |   
bootique-shiro          | bootique-jetty, boutique-jersey, bootique-jdbc    |
bootique-swagger        | bootique-jetty, bootique-jersey                   |    
bootique-kotlin         | bootique-jetty, bootique-logback                  |    
bootique-bom            | all                                               |    


## Hot-fix release instructions

First of all, be cautious with transient dependencies - release of a module requires release of all its dependent modules.
It means that if a module is going to be released as **"hot-fixed"** then its dependent modules must be released too with the same version.
The last step must be a release of *bootique-bom* containing new versions of released modules.
Secondarily, hot fixes are expected to be followed one after another, so that you won’t fall into code mess between hot-fix releases.

1. Create a new branch at a specific tag with
```bash
git checkout -b [branchname] [tagname]
```
E.g. master is 0.25-SNAPSHOT, then create a new branch at a tag for version 0.24.

2. Change branch version to 0.24.1-SNAPSHOT.
3. Fix issues.
4. Perform a release of the branch (and branches for dependent modules).
5. Publish artifact into [Maven Central](https://search.maven.org).
6. Merge the branch into master.

 
