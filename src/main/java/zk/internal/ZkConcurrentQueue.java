package zk.internal;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * Created by morefree on 9/13/14.
 */
public class ZkConcurrentQueue<T> implements ZkQueue<T> {
    @Override
    public void offer(T e) throws IOException, KeeperException, InterruptedException {

    }

    @Override
    public T peek() throws IOException, KeeperException, InterruptedException, ClassNotFoundException {
        return null;
    }

    @Override
    public T poll() throws IOException, KeeperException, InterruptedException, ClassNotFoundException {
        return null;
    }
}
