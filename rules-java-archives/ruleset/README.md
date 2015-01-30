SkipArchives Windup ruleset: Identifies the archives (notably .jar's) from analyzed application
and marks the well known libraries not worth further analyzing to be skipped by other rulesets.
===================================================================================================

The archives are identified by their SHA1 checksum.
Mappings of these checksums to Maven's G:A:V are distributed within the ruleset's addon.
Further mapping can be added to `~/.windup/config/SkipArch` .

Further, this ruleset offers an operation to configure windup to skip user-specified artifacts.


See:

* [Maven indexer examples](https://github.com/cstamas/maven-indexer-examples)

This quickstart demonstrates advanced ruleset authoring with interacting rules and an external dependency.