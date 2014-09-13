package zk.internal;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * Created by kexu1 on 9/11/2014.
 */
public interface ZkQueue <T> {
    void offer(T e) throws IOException, KeeperException, InterruptedException;
    T peek() throws IOException, KeeperException, InterruptedException, ClassNotFoundException;
    T poll() throws IOException, KeeperException, InterruptedException, ClassNotFoundException;
}
