This module contains Docbook documentation for Bootique published on the website.

## Publishing Prerequisites:

Checkout the web site:

```
cd ../   # assuming we were in bootique.git folder, step out
git clone git@github.com:nhl/bootique.git bootique-pages # second copy of Bootique checkout
git checkout -b gh-pages origin/gh-pages
```

## Building and Publishing the Docs:

```
cd <main_bootique_checkout>/bootique-docs
mvn clean package
cp -r target/site/index/ ../../bootique-pages/docs/ 

cd ../../bootique-pages/docs/ 
git add -A
git commit -a -m "docs update"
```

In a few seconds you will be able to check the result at [http://bootique.io/docs/].