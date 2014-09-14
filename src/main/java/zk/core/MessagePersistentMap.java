package zk.core;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by morefree on 9/14/14.
 */
public interface MessagePersistentMap <K, V, T extends Message> extends PersistentMap<K, V>, Consumer<T> {

}
