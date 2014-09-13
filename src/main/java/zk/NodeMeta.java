package zk;

/**
 * serializable class maintained meta data of a cluster.
 * immutable.
 * a node is not necessarily a machine, instead, it might be a process even a thread.
 */
public final class NodeMeta {
    public String ip;

    // TODO  builder pattern
}
