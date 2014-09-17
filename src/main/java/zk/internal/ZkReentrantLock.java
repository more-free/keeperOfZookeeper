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
    static final String GROUP = "lock-";
    static final String PREFIX = "/zk/internal/util/concurrent/reentrant-lock";

    private ZooKeeper zk = ZkManager.getInstance();

    private volatile boolean isPathCreated = false;
    private ThreadLocal<String> name = new ThreadLocal<>();
    private ThreadLocal<Boolean> holdTheLock = new ThreadLocal<>();
    private String lockName = "default";
    private String thisPrefix;

    /**
     * all callers compete for the same lock
     */
    public ZkReentrantLock() {
        init();
    }

    /**
     * compete for the lock with name
     * @param lockName
     */
    public ZkReentrantLock(String lockName) {
        this.lockName = lockName;
        init();
    }

    private void init() {
        thisPrefix = PREFIX + "/" + lockName;
    }

    public String getLockName() {
        return lockName;
    }

    @Override
    public void lock() throws KeeperException, InterruptedException {
        if(checkReentrancy())
            return;  // if already hold the lock, then return instantly

        if(!isPathCreated) {
            synchronized (this) {
                if(!isPathCreated) {
                    try {
                        createRecursively(thisPrefix, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    } catch(KeeperException | InterruptedException e) {
                        // ignore
                    }
                    isPathCreated = true;
                }
            }
        }

        String fullName = zk.create(thisPrefix + "/" + GROUP, null,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        name.set(lastElement(fullName.split("/")));

        waitForLock(name.get(), new CountDownLatch(1));

        holdTheLock.set(true);
    }

    private void waitForLock(String name, CountDownLatch latch) throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(thisPrefix, false);

        if(name.equals(minElement(children).get())) {
            // hold the lock
            latch.countDown();
        } else {
            // set a watcher
            Stat stat = zk.exists(thisPrefix + "/" + prevElement(children, name).get(),
                                  event -> {
                                      if(event.getType() == Watcher.Event.EventType.NodeDeleted) {
                                          try {
                                              waitForLock(name, latch);
                                          } catch (Exception e) {
                                              throw new RuntimeException(e);
                                          }
                                      }
                                  });

            // if the previous node was already gone, then check again
            if(stat == null) {
                waitForLock(name, latch);
            } else {
                latch.await(); // block until receiving the NodeDeleted event of the previous node
            }
        }
    }

    @Override
    public void unlock() throws KeeperException, InterruptedException {
        holdTheLock.set(false);

        zk.delete(thisPrefix + "/" + name.get(), -1);
        name.set(null);
    }

    private boolean checkReentrancy() {
        return holdTheLock.get() != null && holdTheLock.get();
    }

    public static void unlockAll() {
        ZkUtil.removeRecursively(PREFIX, false);
    }

    public static void unlockAll(String lockName) {
        ZkUtil.removeRecursively(PREFIX + "/" + lockName, false);
    }

}
