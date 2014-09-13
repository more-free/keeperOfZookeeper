package zk.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by morefree on 9/13/14.
 */
public class TestZkUtil {
    @Test
    public void testNextSequentialName() {
        String name = "n-0000000001";
        String expectedNextName = "n-0000000002";
        Assert.assertEquals(expectedNextName, ZkUtil.nextSequentialName(name));

        name = "n-" + ZkUtil.MAX_SEQUENTIAL_VALUE;
        expectedNextName = "n-" + ZkUtil.MIN_SEQUENTIAL_VALUE;
        Assert.assertEquals(expectedNextName, ZkUtil.nextSequentialName(name));
    }
}
