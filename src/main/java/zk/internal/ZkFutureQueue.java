package zk.internal;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Created by morefree on 9/13/14.
 */
public class ZkFutureQueue<T> implements ZkQueue<Future<Optional<T>>> {
    @Override
    public void offer(Future<Optional<T>> e) throws IOException, KeeperException, InterruptedException {

    }

    @Override
    public Future<Optional<T>> peek() throws IOException, KeeperException, InterruptedException, ClassNotFoundException {
        return null;
    }

    @Override
    public Future<Optional<T>> poll() throws IOException, KeeperException, InterruptedException, ClassNotFoundException {
        return null;
    }
}
