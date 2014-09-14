package zk.internal;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import zk.ZkManager;
import zk.util.Serializer;
import zk.util.ZkUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static zk.util.ZkUtil.*;

/**
 * Created by morefree on 9/13/14.
 * offer - non-blocking
 * poll - blocking
 * peek - non-blocking
 *
 */
public class ZkBlockingQueue<T> implements ZkQueue<T> {
    private ZooKeeper zk = ZkManager.getInstance();

    static final String PREFIX = "/zk/internal/util/concurrent/blocking-queue";
    static final String GROUP = "n-";
    static final List<ACL> DEFAULT_ACL_LIST = ZooDefs.Ids.OPEN_ACL_UNSAFE;
    static final String NEXT_NODE = "/zk/internal/util/concurrent/helper/blocking-queue/next-node";

    private volatile boolean isPathCreated;
    private Serializer serializer;
    private ZkLock zkLock = new ZkReentrantLock();

    private String queueName = "default";
    private String thisPrefix;
    private String thisNextNode;

    public String getQueueName() {
        return this.queueName;
    }

    public ZkBlockingQueue(Serializer serializer) {
        this.serializer = serializer;
        init();
    }

    public ZkBlockingQueue(Serializer serializer, String queueName) {
        this.serializer = serializer;
        this.queueName = queueName;
        init();
    }

    private void init() {
        thisPrefix = PREFIX + "/" + queueName;
        thisNextNode = NEXT_NODE + "/" + queueName;

        if(!isPathCreated) {
            try {
                createRecursively(thisPrefix, null, DEFAULT_ACL_LIST, CreateMode.PERSISTENT);
                createRecursively(thisNextNode, " ".getBytes(), DEFAULT_ACL_LIST, CreateMode.PERSISTENT);
            } catch(KeeperException | InterruptedException e) {
                // when path already exists exceptions are thrown
                // ignore exceptions
            }
            isPathCreated = true;
        }
    }

    @Override
    public void offer(T t) throws IOException, KeeperException, InterruptedException {
        zk.create(thisPrefix + "/" + GROUP, serializer.serialize(t), DEFAULT_ACL_LIST, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * lock-free (not like poll), but consequently, the returned result may be out-of-date.
     * Formally, it only guarantees eventual consistency.
     * @return null if no node or any exception happens; otherwise return data of the next node of the
     * queue (might be empty)
     */
    @Override
    public T peek() throws ClassNotFoundException, IOException, KeeperException, InterruptedException {
        String nextNodeName = new String(zk.getData(thisNextNode, false, null));
        if(!validName(nextNodeName))  // first peek. queue is empty now.
            return null;

        try {
            return getObjectByNodeName(nextNodeName);
        } catch(KeeperException | InterruptedException e) {
            return null; // next node does not exist for now
        }
    }

    /**
     * "thread safe" with mutual exclusion.
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @Override
    public T poll() throws KeeperException, InterruptedException, ClassNotFoundException, IOException {
        boolean isLockSet = false;
        T target = null;

        try {
            zkLock.lock();
            isLockSet = true;

            // read the name of the next node of the queue. If there is no next node (queue is empty),
            // then it blocks until next node comes in.
            String nextNodeName = new String(zk.getData(thisNextNode, false, null)); // ex. n-000000001

            if (!validName(nextNodeName)) { // first poll. queue is empty now.
                CountDownLatch latch = new CountDownLatch(1);
                List<String> children = zk.getChildren(thisPrefix, event -> {
                   if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged)
                       latch.countDown();
                });

                if(children == null || children.size() == 0) {
                    latch.await();
                    children = zk.getChildren(thisPrefix, false);
                }

                String firstNodeName = children.stream().min(String::compareTo).get();
                nextNodeName = firstNodeName;
                target = getObjectByNodeName(firstNodeName);

            } else {
                try {
                    target = getObjectByNodeName(nextNodeName);
                } catch (KeeperException | InterruptedException e) {
                    // no such node exists. wait for it appears
                    CountDownLatch latch = new CountDownLatch(1);
                    // put a watcher
                    Stat stat = zk.exists(thisPrefix + "/" + nextNodeName, event -> {
                        if (event.getType() == Watcher.Event.EventType.NodeCreated)
                            latch.countDown();
                    });

                    if (stat != null) {
                        // already exists, do not block
                    } else {
                        latch.await();
                    }

                    // get the data
                    target = getObjectByNodeName(nextNodeName);
                }
            }


            // set the name of the next node of the queue
            zk.setData(thisNextNode, nextSequentialName(nextNodeName).getBytes(), -1);
            // dequeue. remove current node from the queue
            zk.delete(thisPrefix + "/" + nextNodeName, -1);
        } finally {
            if(isLockSet)
                zkLock.unlock();
        }

        return target;
    }

    private T getObjectByNodeName(String name)
            throws KeeperException, InterruptedException, IOException, ClassNotFoundException {
        return (T) serializer.deserialize(zk.getData(thisPrefix + "/" + name, false, null));
    }

    private boolean validName(String nodeName) {
        return nodeName != null && nodeName.trim().length() > 0;
    }

    public void clear() {
        deleteAll(this.queueName);
    }

    public static void deleteAll() {
        ZkUtil.removeRecursively(PREFIX, false);
        ZkUtil.removeRecursively(NEXT_NODE, false);
    }

    public static void deleteAll(String queueName) {
        ZkUtil.removeRecursively(PREFIX + "/" + queueName, false);
        ZkUtil.removeRecursively(NEXT_NODE + "/" + queueName, false);
    }
}
