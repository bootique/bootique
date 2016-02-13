This module contains source of the Bootique documentation published on the website. The docs are in Docbook XML format.

## Building the Docs

```shell
cd <main_bootique_checkout>/bootique-docs
mvn clean package
```

You can now inspect the local docs under ```target/site/index/```. If you are not a Bootique maintainer, you may stop here. 

## Publishing Prerequisites

Checkout the web site:

```shell
# going to the parent of <main_bootique_checkout>
cd <main_bootique_checkout>/../

# checkout a second copy of Bootique to be able to copy stuff between the branches
git clone git@github.com:nhl/bootique.git bootique-pages 

# get on the website branch
git checkout -b gh-pages origin/gh-pages
```

## Publishing the Docs

Build the docs locally as described above, and then do this:

```shell

cd <main_bootique_checkout>/bootique-docs
cp -r target/site/index/ ../../bootique-pages/docs/0/
cd ../../bootique-pages/docs/ 
git add -A
git commit -m "docs update"
git push
```

In a few seconds you will be able to check the result at http://bootique.io/docs/ .