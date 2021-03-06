package hashmap;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Hash Table with Tree Set buckets
 * Elements of tree sets need to be comparable, so we restrict our map to
 * only allow comparable keys
 *
 * @author Neil Kulkarni
 */
public class MyHashMapTSBuckets<K extends Comparable<K>, V> extends MyHashMap<K, V> {

    /**
     * Constructor that creates a backing array with default
     * initial size and load factor
     */
    public MyHashMapTSBuckets() {
        super();
    }

    /**
     * Constructor that creates a backing array of initialSize
     * and default load factor
     *
     * @param initialSize initial size of backing array
     */
    public MyHashMapTSBuckets(int initialSize) {
        super(initialSize);
    }

    /**
     * Constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMapTSBuckets(int initialSize, double maxLoad) {
        super(initialSize, maxLoad);
    }

    @Override
    protected Collection<Node> createBucket() {
        // This is fancy new-fangled Java that says in plain English:
        //
        //  "Build a TreeSet of Nodes, and when you compare two Nodes,
        //   compare their keys by their key's compareTo method"
        //
        // Remember, we had K extends Comparable<K> in our class header,
        // so we know the keys have implemented a compareTo method
        return new TreeSet<>(Comparator.comparing(a -> a.key));
    }
}
