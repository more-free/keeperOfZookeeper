package zk.util;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by morefree on 9/12/14.
 * general utils
 */
public class Utils {
    public static <T> T lastElement(T [] array) {
        return array[array.length - 1];
    }

    public static <T extends Comparable<? super T>> Optional<T> minElement(Collection<T> coll) {
        return coll.stream().min((fir, sec) -> fir.compareTo(sec));
    }

    public static <T extends Comparable<? super T>> Optional<T> maxElement(Collection<T> coll) {
        return coll.stream().max((fir, sec) -> fir.compareTo(sec));
    }

    /**
     *
     * @param coll
     * @param <T>
     * @param cur
     * @return previous element which is LESS THAN cur by natural ordering
     */
    public static <T extends Comparable<? super T>> Optional<T> prevElement(Collection<T> coll, T cur) {
        return coll.stream().filter(e -> e.compareTo(cur) < 0).max((fir, sec) -> fir.compareTo(sec));
    }
}
