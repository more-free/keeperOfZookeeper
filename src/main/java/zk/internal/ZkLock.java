package zk.internal;

import org.apache.zookeeper.KeeperException;

/**
 * Created by morefree on 9/11/14.
 */
public interface ZkLock {
    void lock() throws KeeperException, InterruptedException;

    void unlock() throws KeeperException, InterruptedException;
}
