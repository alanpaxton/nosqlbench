# existdb

This is an activity type which allows for the generation of data
into existdb.

## Quick build

We can build the whole of `nb5` slowly, once, installing the components in our local maven repo
```
$ mnv clean install
```
Now for as long as we only develop on our own adapter, we can just rebuild/re-install the adapter
and package it (and verify the package) this way, and we can run again much quicker
```
$ (cd adapter-existdb; mvn clean install)
$ (cd nb5; mvn verify)
$ java -jar ./nb5/target/nb5.jar ./adapter-existdb/target/classes/activities/existdb_crud_basic.yaml driver=existdb
```
and running with the debugger
```
$ java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.01:4005 -jar ./nb5/target/nb5.jar ./adapter-existdb/target/classes/activities/existdb_crud_basic.yaml driver=existdb
```
Then tell IntelliJ to attach to the waiting java process

