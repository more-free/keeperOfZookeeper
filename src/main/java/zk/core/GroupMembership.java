package zk.core;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import zk.NodeMeta;
import zk.ZkManager;
import zk.util.Serializer;
import zk.util.ZkUtil;

import java.io.IOException;
import java.util.List;

/**
 * Created by morefree on 9/14/14.
 *
 * thread-safe
 */
public class GroupMembership {
    private final ZooKeeper zk = ZkManager.getInstance();
    private Serializer serializer;

    private final static String PREFIX = "zk/core/group";
    private final static List<ACL> DEFAULT_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;
    private final static String GROUP = "node-";

    public GroupMembership(Serializer serializer) {
        this.serializer = serializer;
        init();
    }

    public GroupMembership() {
        this.serializer = Serializer.defaultSerializer;
        init();
    }

    private void init() {
        try {
            ZkUtil.createRecursively(PREFIX, null, DEFAULT_ACL, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            if(e instanceof KeeperException.NodeExistsException) {
                // acceptable case
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return  unique path
     */
    public String join(NodeMeta meta)
            throws IOException, KeeperException, InterruptedException {
        return zk.create(PREFIX + "/" + GROUP,
                serializer.serialize(meta), DEFAULT_ACL, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * join with default meta information.
     * default meta is composed of IP + timestamp + random number
     *
     * @return
     */
    public String join() throws IOException, KeeperException, InterruptedException {
        NodeMeta meta = NodeMeta.defaultNodeMeta.with("sig", NodeMeta.signature().getBytes());
        return join(meta);
    }

    public NodeMeta getMeta(String path)
            throws IOException, ClassNotFoundException, KeeperException, InterruptedException {
        return (NodeMeta) serializer.deserialize(zk.getData(path, false, null));
    }

    /**
     * find all nodes in current cluster
     * @return
     */
    public List<String> getAll() throws KeeperException, InterruptedException {
        return zk.getChildren(PREFIX, false);
    }

    public void decommission(String path) throws KeeperException, InterruptedException {
        zk.delete(path, -1);
    }
}
