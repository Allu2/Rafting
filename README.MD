# Rafting

### Setup
dependencies
* json-simple-1.1.1.jar
* JDK 1.8 (oracle)

in folder with compiled/builded jar of the project run
```
export CLASSPATH=Rafting.jar
rmiregistry
```
Make copies of the Rafting.jar in their own folders

```
./
├── Server1
│   └── Rafting_jar
│       ├── config.json
│       ├── log.json
│       └── Rafting.jar -> /home/allu/IdeaProjects/Rafting/out/artifacts/Rafting_jar/Rafting.jar
├── Server2
│   └── Rafting_jar
│       ├── config.json
│       ├── log.json
│       └── Rafting.jar -> /home/allu/IdeaProjects/Rafting/out/artifacts/Rafting_jar/Rafting.jar
└── Server3
    └── Rafting_jar
        ├── config.json
        ├── log.json
        └── Rafting.jar -> /home/allu/IdeaProjects/Rafting/out/artifacts/Rafting_jar/Rafting.jar

```
Write each server their own config.json which includes other servers in the cluster.
then open terminal in each folder and run
```
java -jar Rafting.jar
```

Initializing/resetting the log file can be done by running
```
echo '{"1":{"entry":5, "term":0}}' >log.json

```

The repository includes one test version packed in zip which has config files for 3 servers included.

Rafting has been influenced heavily by https://github.com/ngbalk/Raft
It should be noted however that while most of the structure is similar there lot of differences.

Rafting aims to be implementation of Raft algorithm, unfortunately as it is there is still work to do.

Features that work more or less

* Leader selection
* Log replication (While every request seems to "fail" in the end the logs are identical)

Rafting has not been thoroughly tested and is subject to bugs.



