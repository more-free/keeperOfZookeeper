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

    // static initialization is thread-safe for single class loader
    static {
        init();
    }

    private static void init() {
        try {
             zk = new ZooKeeper("localhost:2181", 5000, null);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
    }

    public static ZooKeeper getInstance() {
        return zk;
    }

    public synchronized static void close() throws InterruptedException {
        if(zk != null)
            zk.close();
    }

    public synchronized static void reopen() {
        init();
    }
}
