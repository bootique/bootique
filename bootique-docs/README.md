This module contains source of the Docbook Bootique documentation published on the website.

## Building the Docs:

```shell
cd <main_bootique_checkout>/bootique-docs
mvn clean package
```

You can now inspect the local docs under ```target/site/index/```. If you are not a Bootique maintainer, you may stop here. 

## Publishing Prerequisites:

Checkout the web site:

```shell
# going to the parent of <main_bootique_checkout>
cd ../../ 

# checkout a second copy of Bootique to be able to copy stuff between the branches
git clone git@github.com:nhl/bootique.git bootique-pages 

# get on the website branch
git checkout -b gh-pages origin/gh-pages
```

## Publishing the Docs:

After you build the docs locally as described above, you can do this:

```shell

# assuming you are in <main_bootique_checkout>/bootique-docs
cp -r target/site/index/ ../../bootique-pages/docs/ 
cd ../../bootique-pages/docs/ 
git add -A
git commit -m "docs update"
git push
```

In a few seconds you will be able to check the result at http://bootique.io/docs/ .