package com.sequenceiq.it.cloudbreak.spark;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkServerPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkServerPool.class);

    private static final long TIMEOUT = 15 * 60 * 1000;

    private final Map<SparkServer, Boolean> servers = new HashMap<>();

    public SparkServer pop() {
        synchronized (servers) {
            SparkServer sparkServer = new SparkServer();
            servers.put(sparkServer, Boolean.FALSE);
            return sparkServer;
        }
    }

    private void logServers() {
        servers.forEach((key, value) -> LOGGER.debug("servers - [{}]", key + "::" + value));
    }

    public void put(SparkServer sparkServer) {
        synchronized (servers) {
            LOGGER.info("Spark server put back. Pool size: {}", servers.entrySet().stream().filter(Entry::getValue).count());
            LOGGER.info("PUT state: {}", sparkServer.getEndpoint());
            logServers();
            servers.put(sparkServer, Boolean.TRUE);
            servers.notify();
        }
    }

    @PreDestroy
    public void autoShutdown() {
        LOGGER.info("Invoking PreDestroy for Spark Pool bean");
        servers.keySet().forEach(SparkServer::stop);
    }
}
