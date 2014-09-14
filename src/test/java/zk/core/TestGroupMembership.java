package zk.core;

import org.junit.AfterClass;
import org.junit.Test;
import zk.NodeMeta;
import zk.ZkManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by morefree on 9/14/14.
 */
public class TestGroupMembership {
    @Test
    public void testGroupMembership() throws Exception {
        GroupMembership.deleteAll();

        GroupMembership g = new GroupMembership();
        String first = g.join(); // join self

        TimeUnit.SECONDS.sleep(1); // wait for coming event
        System.out.println(g.getAll()); // see first

        g.decommission(first);

        TimeUnit.SECONDS.sleep(1); // wait for coming event
        System.out.println(g.getAll()); // see nothing

        first = g.join();  // join self again
        TimeUnit.SECONDS.sleep(1); // wait for coming event
        System.out.println(g.getAll());  // see first

        NodeMeta<String, byte []> meta = NodeMeta.defaultNodeMeta
                .with("name", "second".getBytes());

        String second = g.join(); // join another
        TimeUnit.SECONDS.sleep(2); // wait for coming event
        System.out.println(g.getAll());  // see first, second

        System.out.println(new String((byte []) g.getMeta(second).get("name"))); // see second

        TimeUnit.SECONDS.sleep(2); // wait for coming event
        GroupMembership.deleteAll();
    }

}
