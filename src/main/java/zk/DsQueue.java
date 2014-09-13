package zk;

import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * make use of internal ZkQueue , support batch read / flush
 */
public class DsQueue  {
    private static final String prefix = "zk/util/collections/queue/";

    private ZooKeeper zk;
    private ConcurrentLinkedQueue<String> data;

    private String name;  // queue name, maintained by zookeeper

    public DsQueue() {
        zk = ZkManager.getInstance();
        data = new ConcurrentLinkedQueue<>();

        // TODO join the cluster managed by zookeeper
    }

    public void offer(String val) {
        data.offer(val);
    }

    public void flush() {
     // TODO batch flush
    }
}
