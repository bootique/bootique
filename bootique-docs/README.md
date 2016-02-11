This module contains source of the Docbook Bootique documentation published on the website.

## Publishing Prerequisites:

Checkout the web site:

```shell
# assuming we were in bootique.git folder, step out
cd ../   

# checkout a second copy of Bootique to be able to copy stuff between the branches
git clone git@github.com:nhl/bootique.git bootique-pages 

# get on the website branch
git checkout -b gh-pages origin/gh-pages
```

## Building and Publishing the Docs:

```shell
cd <main_bootique_checkout>/bootique-docs
mvn clean package
cp -r target/site/index/ ../../bootique-pages/docs/ 

cd ../../bootique-pages/docs/ 
git add -A
git commit -m "docs update"
git push
```

In a few seconds you will be able to check the result at http://bootique.io/docs/ .