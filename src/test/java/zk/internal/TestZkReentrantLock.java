package zk.internal;

import org.junit.AfterClass;
import org.junit.Test;
import zk.ZkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by morefree on 9/12/14.
 */
public class TestZkReentrantLock {
    //@Test
    public void testUnlockAll() throws Exception {
        ZkReentrantLock.unlockAll();
        System.out.println(ZkManager.getInstance().getChildren(ZkReentrantLock.PREFIX, false));
    }


    //@Test
    public void testLockWithThreads() throws Exception {
        try {
            testUnlockAll();
        } catch(Exception e) {

        }

        ZkReentrantLock lock = new ZkReentrantLock();
        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            String id = "thread-" + i;
            threads.add(new Thread(() -> {
                try {
                    lock.lock();
                    System.out.println(id + " is holding the lock");

                    lock.lock();
                    TimeUnit.MILLISECONDS.sleep(500);

                    System.out.println(id + " is releasing the lock");
                    lock.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        threads.stream().forEach(e -> e.start());
        threads.stream().forEach(t -> { try{ t.join(); } catch(Exception e){} });
    }
}
