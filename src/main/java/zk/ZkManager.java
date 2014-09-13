package zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by kexu1 on 9/11/2014.
 */
public class ZkManager {
    private static ZooKeeper zk;

    // thread-safe for single class loader
    static {
        try {
            zk = new ZooKeeper("localhost:2181", 5000, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ZooKeeper getInstance() {
        return zk;
    }

    // return a unique id stands for the node
    public static Integer join(NodeMeta meta) {
        return null; // TODO
    }

    public static Boolean decommission(Integer id) {
        return null; // TODO
    }

    public synchronized static void close() throws InterruptedException {
        if(zk != null)
            zk.close();
    }

    public static void main(String [] args) throws Exception {
        ZooKeeper zk = getInstance();

        zk.close();
    }
}
