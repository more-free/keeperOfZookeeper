package zk;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import zk.core.TestGroupMembership;

/**
 * Created by morefree on 9/14/14.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestGroupMembership.class, TestGroupMembership.class })
public class TestSuite {

    @AfterClass
    public static void cleanUp() {
        try {
            ZkManager.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
