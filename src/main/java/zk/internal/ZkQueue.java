package zk.internal;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by kexu1 on 9/11/2014.
 */
public interface ZkQueue <T> {
    void offer(T e) throws IOException, KeeperException, InterruptedException;
    T peek() throws IOException, KeeperException, InterruptedException;
    T poll() throws IOException, KeeperException, InterruptedException;
}
