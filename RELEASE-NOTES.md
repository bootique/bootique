## 0.13:

* #14 Support for configs polymorphism

## 0.12:

* #17 Wrap CLI Options API to avoid direct dependency on JOpt lib in commands
* #18 Separate a concept of DI-bound option from Command
* #19 Command dispatch mechanism based on Cli state
* #20 Allow configuration of available commands in main()
* #21 Move contribution API into static methods on BQCoreModule
* #22 Better Module override API
* #24 Allow creation of DefaultBootLogger with non-System stdin/out
* #28 --config option should support both files and URLs

## 0.11:

* #8 Remove API's deprecated since 0.10
* #10 Service shutdown functionality

## 0.10:

* #5 FactoryConfigurationService refactoring
* #6 Service/Module override support
* #7 Start publishing Bootique to Maven central repo

## 0.9:

* #4 FactoryConfigurationService - support for loading parameterized types

## 0.8:

* #1 Merge bundles and modules into a single concept
* #2 Auto-load extensions
* #3 Create BootLogger outside DI, add "trace" logging
