package com.containersolutions.mesos;

import com.jayway.awaitility.Awaitility;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

@Category(SystemTest.class)
public class StartupTests extends TestBase {
    protected final Log logger = LogFactory.getLog(getClass());

    @Test
    public void shouldStartMesosMaster() {
        Awaitility.await().pollInterval(1L, TimeUnit.SECONDS).atMost(60L, TimeUnit.SECONDS).until(() -> {
            try {
                logger.debug(Unirest.get(cluster.getMasterContainer().getStateUrl()).asJson().getBody());
                return true;
            } catch (UnirestException e) {
                return false;
            }
        });
    }

//
//    @Test
//    public void shouldDoSomething() {
//        String zookeeper = cluster.getZkContainer().getIpAddress() + ":2181";
//        System.out.println(zookeeper);
//        SystemTestApplication.main(new String[]{"--mesos.zookeeper.server=" + zookeeper});
//        logger.debug(cluster.getContainers().stream().map(container -> container.getName()).collect(Collectors.joining(", ")));
//    }
}
