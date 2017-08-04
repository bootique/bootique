# Bootique Release Guide

Here you can find guidelines for performing a release. The important thing is that Bootique consists of a number of modules dependent between themselves. 
Therefore in the process of release it’s necessary to stick to the order of modules described in the **Release dependency groups**.

## Prerequisites 

1. Generate a GPG key to sign commits and tags. More info [Generating a new GPG key](https://help.github.com/articles/generating-a-new-gpg-key/), 
[Adding a new GPG key to your GitHub account](https://help.github.com/articles/adding-a-new-gpg-key-to-your-github-account/)

2. Distribute your key. Sent a key to a keyserver using the command-line. 
See [The GNU Privacy Handbook](https://www.gnupg.org/gph/en/manual/x457.html) for details.

3. To connect and authenticate to GitHub server, use SSH key.  
Look at [Generating a new SSH key and adding it to the ssh-agent](https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/).

4. For deploying of maven artifacts to a remote repository [https://api.bintray.com/maven/bootique/releases](https://api.bintray.com/maven/bootique/releases),  
make sure "bintray-bootique-releases" repository is configured in the *pom.xml* within `distributionManagement` element and the authentication information 
for connecting to the repository in the *~/.m2/settings.xml*. 
See [this page](http://www.apache.org/dev/publishing-maven-artifacts.html) for details. 
Sample *settings.xml*:
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
**NOTE:** The following steps *prepare sources* and *release maven artifact* are performed cyclically till the last module on the list is released.

## Prepare sources

1. Open *pom.xml* of a module and upgrade related Bootique modules on the newest release version.
2. Build the module to reveal issues of incompatibility.
```bash
mvn install
```
3. Fix issues and track them on GitHub if upgrade led to API or serious code changes. 
4. Edit RELEASE-NOTES.md if there is anything to add there.
5. Push changes on GitHub.

## Release Maven Artifact

The artifact is released in 2 stages: preparing the release and performing the release.

* Preparing the release
```bash
mvn release:prepare -Pgpg
```
* Performing the release
```bash
mvn release:perform -Pgpg
```
Finally, an artifact is deployed to the local repository and to a remote repository configured the pom.xml. 
Check info about a new module release on [https://bintray.com/bootique/releases](https://bintray.com/bootique/releases).	

## Maven Central Synchronization

When all modules are released and available in [jCenter](https://bintray.com/bintray/jcenter),  the last thing is to publish them into [Maven Central](https://search.maven.org). 
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
bootique-linkrest       | bootique-jersey, bootique-cayenne                 |
bootique-jersey-client  | bootique-jersey, bootique-logback                 |
bootique-mvc            | bootique-jetty, bootique-jersey                   |   
bootique-shiro          | bootique-jetty, boutique-jersey, bootique-jdbc    |
bootique-swagger        | bootique-jetty, bootique-jersey                   |    
bootique-kotlin         | bootique-jetty, bootique-logback                  |    
bootique-bom            | all                                               |    



