package zk.core;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by morefree on 9/14/14.
 */
public interface PersistentMap <K, V> extends Persistentable {
    void put(K key, V val);
    void putIfAbsent(K key, V val);
    Optional<V> get(K key);
}

