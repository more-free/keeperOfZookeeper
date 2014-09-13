package zk.internal;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import zk.ZkManager;
import zk.util.Serializer;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import static zk.util.ZkUtil.createRecursively;

/**
 * Created by morefree on 9/13/14.
 */
public class ZkBlockingQueue<T> implements ZkQueue<T> {
    private ZooKeeper zk = ZkManager.getInstance();

    static final String PREFIX = "/zk/internal/util/concurrent/blocking-queue";
    static final String GROUP = "n-";
    static final List<ACL> DEFAULT_ACL_LIST = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    private volatile boolean isPathCreated;
    private ThreadLocal<QueueNode> node = new ThreadLocal<>();
    private Serializer serializer;

    public ZkBlockingQueue(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void offer(T t) throws IOException, KeeperException, InterruptedException {
        if(!isPathCreated) {
            synchronized (this) {
                if(!isPathCreated) {
                    try {
                        createRecursively(PREFIX, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    } catch(KeeperException | InterruptedException e) {
                        // ignore
                    }
                    isPathCreated = true;
                }
            }
        }

        zk.create(PREFIX + "/" + GROUP, serializer.serialize(t), DEFAULT_ACL_LIST, CreateMode.PERSISTENT);
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public T poll() {
        return null;
    }

    private static class QueueNode {
        String name;
        byte [] data;

        QueueNode(String name, byte [] data) {
            this.name = name;
            this.data = data;
        }
    }
}
