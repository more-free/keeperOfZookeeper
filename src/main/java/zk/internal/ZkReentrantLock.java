package zk.internal;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import zk.ZkManager;
import zk.util.ZkUtil;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static zk.util.ZkUtil.*;
import static zk.util.Utils.*;

/**
 * Created by morefree on 9/11/14.
 *
 * Its behavior is just like the "synchronized" keyword
 */
public class ZkReentrantLock implements ZkLock {
    private ZooKeeper zk = ZkManager.getInstance();
    private static final String prefix = "/zk/internal/util/concurrent/lock";

    private volatile boolean isPathCreated = false;
    private ThreadLocal<String> name = new ThreadLocal<>();
    private ThreadLocal<Boolean> holdTheLock = new ThreadLocal<>();

    @Override
    public void lock() throws KeeperException, InterruptedException {
        if(holdTheLock.get() != null && holdTheLock.get())
            return;  // if already hold the lock, then return instantly

        if(!isPathCreated) {
            synchronized (this) {
                if(!isPathCreated) {
                    try {
                        createRecursively(prefix, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    } catch(KeeperException | InterruptedException e) {
                        // ignore
                    }
                    isPathCreated = true;
                }
            }
        }

        String fullName = zk.create(prefix + "/lock-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        name.set(lastElement(fullName.split("/")));

        List<String> children = zk.getChildren(prefix, false);
        Collections.sort(children);

        if(name.get().equals(children.get(0))) {
            // hold the lock - simply do nothing and return
        } else {
            // set a watcher
            int prev = children.indexOf(name.get()) - 1;

            CountDownLatch latch = new CountDownLatch(1);
            Stat stat = zk.exists(prefix + "/" + children.get(prev), event -> {
                 if(event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    latch.countDown();
                 }
            });

            // if the previous node is already gone, then hold the lock directly
            // this case indicates the previous node already release its lock (and then remove the znode explicitly)
            if(stat == null) {

            } else {
                latch.await(); // block until receiving the NodeDeleted event of the previous node
            }
        }

        holdTheLock.set(true);
    }

    @Override
    public void unlock() throws KeeperException, InterruptedException {
        holdTheLock.set(false);

        zk.delete(prefix + "/" + name.get(), -1);
        name.set(null);
    }

    public static void unlockAll() {
        ZkUtil.removeRecursively(prefix, false);
    }

}
