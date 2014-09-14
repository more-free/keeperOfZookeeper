package zk.core;

/**
 * Created by morefree on 9/14/14.
 */
public interface Message <K, V> {
    K id();
    V content();
}
