package zk.internal;

import org.apache.zookeeper.KeeperException;
import zk.util.Serializer;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by morefree on 9/13/14.
 */
public class ZkFutureQueue<T> {
    private ExecutorService executorService;
    private String queueName;
    private Serializer serializer;
    private ZkBlockingQueue<T> blockingQueue;


    public ZkFutureQueue(ExecutorService executorService, Serializer serializer) {
        this.executorService = executorService;
        this.serializer = serializer;
        this.blockingQueue = new ZkBlockingQueue<T>(serializer);
    }

    public ZkFutureQueue(ExecutorService executorService, Serializer serializer, String queueName) {
        this.queueName = queueName;
        this.executorService = executorService;
        this.serializer = serializer;
        this.blockingQueue = new ZkBlockingQueue<T>(serializer, queueName);
    }

    public void offer(T t) throws IOException, KeeperException, InterruptedException {
        blockingQueue.offer(t);
    }

    public T peek() throws IOException, KeeperException, InterruptedException, ClassNotFoundException {
        return blockingQueue.peek(); // for now blockingQueue has non-blocking peek()
    }

    public Future<T> poll() throws IOException, KeeperException, InterruptedException, ClassNotFoundException {
        return executorService.submit(() -> { return blockingQueue.poll(); });
    }
}
