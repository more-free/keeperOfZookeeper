package zk.core;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import zk.NodeMeta;
import zk.ZkManager;
import zk.util.Serializer;
import zk.util.ZkUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by morefree on 9/14/14.
 *
 * thread-safe.
 */
public class GroupMembership {
    static String PREFIX = "/zk/core/group";
    static List<ACL> DEFAULT_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;
    static String GROUP = "node-";

    private final ZooKeeper zk = ZkManager.getInstance();
    private Serializer serializer;
    private String thisPrefix;
    private String groupName = "default";

    private List<String> members;
    private Watcher groupWatcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if(event.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    sync();
                } catch (Exception e) {
                    // log it
                }
            }
        }
    };

    public GroupMembership() {
        this.serializer = Serializer.defaultSerializer;
        init();
    }

    public GroupMembership(String groupName) {
        this.groupName = groupName;
        this.serializer = Serializer.defaultSerializer;
        init();
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    private void init() {
        members = new ArrayList<>();
        thisPrefix = PREFIX + "/" + groupName;

        try {
            ZkUtil.createRecursively(thisPrefix, null, DEFAULT_ACL, CreateMode.PERSISTENT);
            sync();
        } catch (KeeperException | InterruptedException e) {
            if(e instanceof KeeperException.NodeExistsException) {
                // acceptable case
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * join self or any node with specified meta data
     * @return  unique path
     */
    public String join(NodeMeta meta)
            throws IOException, KeeperException, InterruptedException {
        return zk.create(thisPrefix + "/" + GROUP,
                serializer.serialize(meta), DEFAULT_ACL, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * join self with default meta information.
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
     * find all nodes in current group. NOTE the returned list may already out-of-date.
     * @return current group members without ordering
     */
    public List<String> getAll() throws KeeperException, InterruptedException {
        List<String> copy = new ArrayList<>();
        copy.addAll(members);
        return copy;
    }

    /**
     * sync local group membership with zookeeper server.
     * this might also change the result of getAll().
     */
    public void sync() throws KeeperException, InterruptedException {
        members = zk.getChildren(thisPrefix, groupWatcher, null); // also reset the watcher
    }

    public void decommission(String path) throws KeeperException, InterruptedException {
        zk.delete(path, -1);
    }

    public static void deleteAll() {
        ZkUtil.removeRecursively(PREFIX, false);
    }

    public static void deleteAll(String groupName) {
        ZkUtil.removeRecursively(PREFIX + "/" + groupName, false);
    }
}
