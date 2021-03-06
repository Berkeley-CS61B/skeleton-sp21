package hashmap;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Hash Table with Priority Queue buckets
 * Elements of priority queues need to be comparable, so we restrict our map to
 * only allow comparable keys
 *
 * @author Neil Kulkarni
 */
public class MyHashMapPQBuckets<K extends Comparable<K>, V> extends MyHashMap<K, V> {

    /**
     * Constructor that creates a backing array with default
     * initial size and load factor
     */
    public MyHashMapPQBuckets() {
        super();
    }

    /**
     * Constructor that creates a backing array of initialSize
     * and default load factor
     *
     * @param initialSize initial size of backing array
     */
    public MyHashMapPQBuckets(int initialSize) {
        super(initialSize);
    }

    /**
     * Constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMapPQBuckets(int initialSize, double maxLoad) {
        super(initialSize, maxLoad);
    }

    @Override
    protected Collection<Node> createBucket() {
        // This is fancy new-fangled Java that says in plain English:
        //
        //  "Build a PriorityQueue of Nodes, and when you compare two Nodes,
        //   compare their keys by their key's compareTo method"
        //
        // Remember, we had K extends Comparable<K> in our class header,
        // so we know the keys have implemented a compareTo method
        return new PriorityQueue<>(Comparator.comparing(a -> a.key));
    }
}
