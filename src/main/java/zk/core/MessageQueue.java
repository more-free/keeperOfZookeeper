package zk.core;

import java.util.function.Consumer;

/**
 * Created by morefree on 9/14/14.
 *
 * Consumer interface could be easily used to compose a pipeline,
 * which (IMO) is more flexible than decorator pattern.
 *
 */
public interface MessageQueue<T extends Message> extends Consumer<T> {
    void offer(T t);
    T peel();
    T poll();
}
