# Spring Boot starter for Mesos

[![Join the chat at https://gitter.im/ContainerSolutions/mesos-starter](https://badges.gitter.im/ContainerSolutions/mesos-starter.svg)](https://gitter.im/ContainerSolutions/mesos-starter?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Spring Boot starter package for writing Mesos frameworks

## Features
- Vertical scaling
- Deploy executor on all slaves
- Support for Docker containers

## Getting Started
Start by adding the `spring-boot-starter-mesos` dependency to your project

```
<dependency>
    <groupId>com.github.containersolutions.mesos-starter</groupId>
    <artifactId>spring-boot-starter-mesos</artifactId>
    <version>0.1</version>
</dependency>
```

Add a configuration file (`.properties` or `.yml` format) to your resources folder, or pass the configuration in via CLI parameters or environmental variables. See [Configuration](#Configuration).

Create a main class that starts the application: 

```
@SpringBootApplication
public class SampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(SampleApplication.class, args);
  }
}
```

For a complete example, see `mesos-starter-sample` module.

# Configuration
Most of an application's boilerplate is provided my Mesos-Starter. To configure that boilerplate, there are a range of configuration options. This table describes those options, whether they are optional and whether they have a default value.

Command                         | Description                                           | Default               | Required
---                             | ---                                                   | ---                   | ---
`spring.application.name`       | Application name                                      |                       | Yes
`mesos.framework.name`          | Framework name used in Mesos and ZooKeeper            |                       | Yes
`mesos.master`                  | Path to mesos master                                  |                       | Yes
`mesos.zookeeper.server`        | IP:PORT of the zookeeper server                       |                       | Yes
`mesos.resources.cpus`          | CPUs allocated to the task                            | Accept all            |
`mesos.resources.mem`           | RAM allocated to the task                             | Accept all            |
`mesos.resources.count`         | Number of task instances                              | As many as possible   |
`mesos.resources.ports`         | A map of port mappings                                |                       |
`mesos.resources.distinctSlave` | Reject offers from hosts that already have a task     | `false`               |
`mesos.role`                    | Reject offers that are not offered to this role       | `*`                   |
`mesos.docker.image`            | Docker image to use                                   |                       |
`mesos.docker.network`          | Type of docker network                                | `BRIDGE`              |
`mesos.command`                 | Set the docker CMD or shell command                   |                       |
`mesos.uri`                     | A list of files to download into the Mesos sandbox    |                       |
`mesos.principal`               | The Mesos principal for framework authentication      |                       |
`mesos.secret`                  | The Mesos secret for framework authentication         |                       | 

## Mesos master address format
The `mesos.master` setting accepts any of the following formats: `host:port`, `zk://host1:port1,host2:port2,.../path`, `zk://username:password@host1:port1,host2:port2,.../path` or `file:///path/to/file`

## Port Mapping
The ports requirement serves two purposes. Requesting port resources from Mesos and mapping them to the application. The port settings must follow the format:
```
mesos.resources.ports.[NAME].{host,container}={ANY,UNPRIVILEGED,PRIVILEGED,[INTEGER]}
```
Where:
- `[NAME]` will be used for the (upper case) environmental variable
- `host` refers to the port number on the host machine
- `container` refers to the port inside the container
- `ANY`, `UNPRIVILEGED` will reserve any port of 1025 or above
- `PRIVILEGED` will only reserve any port of 1024 or below
- `[INTEGER]` *Any positive number*, a fixed port, if available.

For example

```
mesos.resources.ports.http.host=ANY
mesos.command=runwebserver.sh --port=$HTTP
```

This will obtain any unprivileged port from Mesos, and expose it in an environment variable named `$HTTP`.

When running with containers you can add the `.container` to map it to a container port when running in bridge mode. For example:

```
mesos.resources.ports.http.host=ANY
mesos.resources.ports.http.container=80
mesos.docker.network=BRIDGE
mesos.docker.image=tutum/hello-world
```

This will reserve any port above 1024 and let docker map it to port 80 on the container.

## Uris
Mesos is able to download files into the Mesos sandbox, for use with your application. It must be passed as a list, as there may be more than one uri to download. For example in properties format:

```
mesos.uri[0]="http://file.com/path/to/file.zip"
mesos.uri[1]="http://file.com/path/to/config.properties"
```
or in yml format:
```
mesos:
    uri:
        - "http://file.com/path/to/file.zip"
        - "http://file.com/path/to/config.properties"
```
These files will then be available in the location specified by the environmental variable `$MESOS_SANDBOX`.

# Use cases

A few good examples

## Stateless web application
For a stateless web application that can run anywhere in the cluster with only a requirement for a single network port, the following should be sufficient

```
mesos.resources.count=3
mesos.resources.cpus=0.1
mesos.resources.mem=64
mesos.resources.ports=1
```

This will run 3 instances of the application with one port exposed. Bare in mind that they all might run on the very same host.

## Distributed database application
For a distributed database you want to run a certain number of instances and never more than one on every host. To achieve that you can enable `count` and `distinctSlave`, like

```
mesos.resources.count=3
mesos.resources.distinctSlave=true
mesos.resources.cpus=0.1
mesos.resources.mem=64
mesos.resources.ports=1
```

## Cluster wide system daemon
Often operations would like to run a single application on each host in the cluster to harvest information from every single node. This can be achieved by not adding the instances count rule and adding the Distinct slave rule.

```
mesos.resources.distinctSlave=true
mesos.resources.cpus=0.1
mesos.resources.mem=64
```

Another, safer, way to achieve the same result is by assigning resources to a specific role on all nodes. I.e. by adding the following to `/etc/mesos-slave/resources`
```
cpus(sampleDaemon):0.2; mem(sampleDaemon):64; ports(sampleDaemon):[514-514];
```

And configure the scheduler with the following options

```
mesos.role=sampleDaemon
mesos.resources.distinctSlave=true
mesos.resources.role=all
```

This way the scheduler will take all resources allocated to the role and make sure it's only running once in every single slave.

It is always recommended to run the scheduler with a Mesos role and reserved resources in such cases to make sure that scheduler is being offered resources for all nodes in the cluster.

# Framework shutdown
The tasks will restart if they receive a `SIGKILL` or are lost (system crashes etc.). If you want to completely de-register the framework and shutdown all tasks, just stop the scheduler with a plain `SIGTERM`.

# Debugging
If you have any problems, then you can alter the log level in the application and in Spring. For example adding this to the CLI: `--logging.level.com.containersolutions.mesos=DEBUG --debug` will start the framework and Spring in debug mode. The framework will accept the values:`DEBUG`, `INFO`, `WARN`, `ERROR`.
