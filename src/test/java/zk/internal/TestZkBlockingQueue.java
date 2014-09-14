package zk.internal;

import org.junit.AfterClass;
import org.junit.Test;
import zk.ZkManager;
import zk.util.Serializer;

import java.util.concurrent.TimeUnit;

/**
 * Created by morefree on 9/13/14.
 */
public class TestZkBlockingQueue {
    @Test
    public void testZkBlockingQueue() throws Exception {
        ZkReentrantLock.unlockAll();
        ZkBlockingQueue.deleteAll();

        ZkQueue<String> queue = new ZkBlockingQueue<>(Serializer.defaultSerializer);

        new Thread(() -> {
            try {
                String next = queue.poll();
                System.out.println(next);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        TimeUnit.SECONDS.sleep(2);

        new Thread(() -> {
            try {
                queue.offer("element 1");
                System.out.println("offered element 1");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                queue.offer("element 2");
                System.out.println("offered element 2");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                String next = queue.poll();
                System.out.println(next);

                next = queue.poll();
                System.out.println(next);

            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        TimeUnit.SECONDS.sleep(1);

        new Thread(() -> {
            try {
                queue.offer("element 3");
                System.out.println("offered element 3");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();

        TimeUnit.SECONDS.sleep(5);

    }
}
