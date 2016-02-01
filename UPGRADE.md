0.12:

* Command API has been redone. If you have modules or apps that implement Commands, 
  you will need to update those. Sample commands can be found in our modules, e.g. 
  [bootique-jobs](https://github.com/nhl/bootique-job/tree/master/src/main/java/com/nhl/bootique/job/command).

  
* @Args annotation was moved to "com.nhl.bootique.annotation" package from "com.nhl.bootique.jopt". 
  Update your imports.

* Now only one command can match command line options. THis was true before, but wasn't enforced.
  So you may see certain command line configs failing with errors, where they appeared to work before
  by pure chance.