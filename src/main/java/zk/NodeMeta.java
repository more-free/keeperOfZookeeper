package zk;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * serializable class maintained meta data of a cluster.
 * immutable.
 * a node is not necessarily a machine, instead, it might be a process even a thread.
 */
public interface NodeMeta <K, V> {

    NodeMeta with(K key, V val);
    V get(K key);
    void del(K key);

    public static Random rnd = new Random(System.currentTimeMillis());

    public static String signature() {
        String hostName = "unknownHost";
        try {
            hostName = InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException e) {

        }

        String timestamp = System.currentTimeMillis() + "";
        String random = rnd.nextLong() + "";

        return hostName + "-" + timestamp + "-" + random;
    }

    public static NodeMeta<String, byte []> defaultNodeMeta = new DefaultNodeMeta();

    public static enum ATTRS {
        IP("ip"),
        PORT("port"),
        RND_NUM("rnd_num"),
        TIME("time");

        private String attrName;
        ATTRS(String attrName) {
            this.attrName = attrName;
        }
    }
}

final class DefaultNodeMeta implements NodeMeta<String, byte []>, Serializable {
        private Map<String, byte []> map = new HashMap<>();

        @Override
        public NodeMeta with(String key, byte [] val) {
            map.put(key, val);
            return this;
        }

        @Override
        public byte [] get(String key) {
            return map.get(key);
        }

        @Override
        public void del(String key) {
            map.remove(key);
        }
}
