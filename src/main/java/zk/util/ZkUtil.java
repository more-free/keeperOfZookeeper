package zk.util;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import zk.ZkManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Created by kexu1 on 9/11/2014.
 */
public class ZkUtil {
    private static ZooKeeper zk = ZkManager.getInstance();

    public static final Integer MAX_SEQUENTIAL_VALUE = 2147483647;
    public static final Integer MIN_SEQUENTIAL_VALUE = -2147483647;

    public static void createRecursively(String path, byte [] data, List<ACL> aclList, CreateMode mode)
            throws KeeperException, InterruptedException {
        List<String> nodes = split(path);

        String prefix = nodes.stream().limit(nodes.size() - 1).reduce("", (x, y) -> {
            String parent = x + "/" + y;
            try {
                zk.create(parent, null, aclList, CreateMode.PERSISTENT);
            } catch (Exception e) {
                // ignore exception silently
            }
            return parent;
        });

        zk.create(prefix + "/" + nodes.get(nodes.size() - 1), data, aclList, mode);
    }

    /**
     * Create parents node only, leaving the last znode.
     * by default parent nodes are persistent and has no related data
     * @param path
     * @param aclList
     */
    public static void createParents(String path, List<ACL> aclList) {
        List<String> nodes = split(path);

        nodes.stream().limit(nodes.size() - 1).reduce("", (x, y) -> {
            String parent = x + "/" + y;
            try {
                zk.create(parent, null, aclList, CreateMode.PERSISTENT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return parent;
        });
    }

    /**
     * ex.  remove  /foo/bar/x :
     * if x has children, then remove all nodes under the path /foo/bar/x/*, finally remove /foo/bar/x
     * otherwise, remove /foo/bar/x directly
     * @param path it must have the format /foo/bar/x
     * @param removeSelf  false : just remove all children, keep itself
     */
    public static void removeRecursively(String path, boolean removeSelf) {
        List<String> children = new ArrayList<>();
        try {
            children = zk.getChildren(path, false);
        } catch(Exception e) {
            // ignore empty znode
        }
        if(!children.isEmpty()) {
            children.stream().forEach(c -> {
                try {
                    ZKUtil.deleteRecursive(zk, path + "/" + c);
                } catch(KeeperException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if(removeSelf) {
            try {
                zk.delete(path, -1);
            } catch(KeeperException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void removeRecursively(String path) {
        removeRecursively(path, true);
    }

    /**
     * @param name node-0000000001
     * @return node-0000000002
     */
    public static String nextSequentialName(String name) {
        String [] parts = name.split("-");
        if(parts.length == 3) parts[1] = "-" + parts[2]; // negative number

        Integer val = Integer.parseInt(parts[1]);
        if(val.equals(MAX_SEQUENTIAL_VALUE))
            return parts[0] + "-" + MIN_SEQUENTIAL_VALUE;
        else
            return parts[0] + "-" + String.format("%010d", val + 1);
    }

    private static List<String> split(String path) {
        return Arrays.asList(path.split("/")).stream().filter(s -> s.length() > 0).collect(Collectors.toList());
    }
}
