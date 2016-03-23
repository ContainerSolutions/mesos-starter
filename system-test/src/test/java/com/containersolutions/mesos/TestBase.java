package com.containersolutions.mesos;

import com.containersol.minimesos.cluster.MesosCluster;
import com.containersol.minimesos.mesos.ClusterArchitecture;
import com.containersolutions.mesos.containers.MesosMasterTagged;
import com.containersolutions.mesos.containers.MesosSlaveTagged;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestBase {
    public static final Integer TIMEOUT = 300;
    protected static MesosCluster cluster;
    private static final List<Integer> ports = IntStream.range(31000, 31003).boxed().collect(Collectors.toList());
    private static final List<String> resources = ports.stream()
            .map(port -> "ports(*):" + "[" + port + "-" + port + "]; " + "cpus(*):1.0; mem(*):512; disk(*):200")
            .collect(Collectors.toList());

    @ClassRule
    public static final TestWatcher WATCHER = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            cluster.stop();
        }
    };

    @BeforeClass
    public static void reinitialiseCluster() {
        ClusterArchitecture.Builder builder = new ClusterArchitecture.Builder()
                .withZooKeeper()
                .withMaster(MesosMasterTagged::new);
        resources.forEach(resource -> builder.withAgent(zooKeeper -> new MesosSlaveTagged(zooKeeper, resource)));

        cluster = new MesosCluster(builder.build());
        cluster.setExposedHostPorts(true);
        cluster.start(TIMEOUT);
//        IpTables.apply(CLUSTER_ARCHITECTURE.dockerClient, cluster, TEST_CONFIG);
    }

    //    @BeforeClass
//    public static void prepareCleanDockerEnvironment() {
//        new DockerUtil(dockerClient).killAllSchedulers();
//        new DockerUtil(dockerClient).killAllExecutors();
//    }
//
    @AfterClass
    public static void killAllContainers() {
        cluster.stop();
//        new DockerUtil(dockerClient).killAllExecutors();
    }
}
