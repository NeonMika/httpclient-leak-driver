package at.jku.ssw;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import java.util.ArrayList;
import java.util.List;

public class FullMemoryLeakExample {

    private static final int REQUESTS_PER_BATCH = 10_000;
    private static final int BATCH_COUNT = 8;
    private static final int WAIT_TIME = 5_000;
    private static int port = 1;

    private static void performRequests(MultiThreadedHttpConnectionManager man) {
        List<HttpConnection> connections = new ArrayList<>();
        for (int i = 0; i < REQUESTS_PER_BATCH; i++) {
            HostConfiguration hostConfiguration = new HostConfiguration();
            // we can supply a dummy value here, because we dont actually open the connection
            // it is enough to use different ports, as that is an easy way to create a lot of host configurations, for which equals returns false
            hostConfiguration.setHost("DUMMY_VALUE", port++);
            HttpConnection connection = man.getConnection(hostConfiguration);
            connections.add(connection);
        }

        MemoryInfoHelper.performGCAndPrintInfo("REQUESTS CREATED");
        justWait();

        // release our 'fake' connections - they are not the target,
        // but rather the HostConfigurations, that are not disposed until the connection manager is shutdown
        for (int i = 0; i < REQUESTS_PER_BATCH; i++) {
            connections.get(i).releaseConnection();
        }
        connections.clear();
        MemoryInfoHelper.performGCAndPrintInfo("AFTER RELEASING REQUESTS");
        justWait();

        man.deleteClosedConnections();
        MemoryInfoHelper.performGCAndPrintInfo("AFTER DELETING CLOSED REQUESTS");
    }

    private static void justWait() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        justWait();
        MemoryInfoHelper.performGCAndPrintInfo("STARTUP");
        justWait();
        MultiThreadedHttpConnectionManager man = new MultiThreadedHttpConnectionManager();
        man.getParams().setMaxTotalConnections(BATCH_COUNT * REQUESTS_PER_BATCH);
        MemoryInfoHelper.performGCAndPrintInfo("BEFORE BATCHES");
        justWait();
        for (int i = 0; i < BATCH_COUNT; i++) {
            performRequests(man);
            justWait();
            System.out.println("FINISHED BATCH " + i);
        }
        justWait();
        man.shutdown();
        man = null;
        MemoryInfoHelper.performGCAndPrintInfo("MAN SHUTDOWN");
        justWait();
        MemoryInfoHelper.performGCAndPrintInfo("EXITING");
    }

}
