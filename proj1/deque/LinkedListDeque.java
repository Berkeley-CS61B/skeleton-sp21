package deque;
import java.util.Iterator;

/** The class for Deque.
 *  @author Cao Yuxin
 */
public class LinkedListDeque<T> implements Iterable<T> {
    private int size;
    Node sentinel;
    public class Node {
        public T item;
        public Node next; // 引用类型
        public Node prev;
        public Node(T i, Node p, Node n) {
            item = i;
            next = n;
            prev = p;
        }
        public Node() {
            item = null;
            next = null;
            prev = null;
        }
    }

    // Deque的迭代器
    class DequeIterator<T> implements Iterator<T> {
        LinkedListDeque<T>.Node current;
        LinkedListDeque<T>.Node _sentinel; // 哨兵节点

        // initialize pointer to head of the list for iteration
        public DequeIterator(LinkedListDeque<T> list)
        {
            current = list.sentinel;
            _sentinel = list.sentinel;
        }

        // returns false if next element does not exist
        public boolean hasNext()
        {
            return current.next != _sentinel;
        }

        // return current data and update pointer
        public T next()
        {
            T data = current.item;
            current = current.next;
            return data;
        }
    }
    public  LinkedListDeque() {
        size = 0;
        Node head =new Node();
        head.prev = head;
        head.next =head;
        sentinel = head;
    }
    // Adds an item of type T to the front of the deque.
    // Item is never null.
    public void addFirst(T item) {
        Node first = new Node(item, sentinel, sentinel.next);
        sentinel.next.prev = first;
        sentinel.next = first;
        size++;
    }

    //  Adds an item of type T to the back of the deque.
    //  Item is never null.
    public void addLast(T item) {
        Node last = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.next = last;
        sentinel.prev = last;
        size++;
    }

    // Returns true if deque is empty, false otherwise.
    public boolean isEmpty() {
        return size == 0;
    }

    // the number of items in the deque
    public int size(){
        return size;
    }

    // Prints the items in the deque from first to last,
    // separated by a space.
    // Once all the items have been printed, print out
    // a new line.
    public void printDeque() {
        Node n = sentinel.next;
        while(n != sentinel){
            System.out.print(n.item + " ");
            n = n.next;
        }
        System.out.println();
    }

    // Removes and returns the item at the front of the
    // deque. If no such item exists, returns null.
    public T removeFirst() {
        if (size == 0){
            return null;
        }
        Node newFirst = sentinel.next.next;
        Node oldFirst = sentinel.next;
        newFirst.prev = sentinel;
        sentinel.next = newFirst;
        size--;
        return oldFirst.item;
    }
    // Removes and returns the item at the back of the
    // deque. If no such item exists, returns null.
    public T removeLast(){
        if (size == 0) { return null; }
        T lastItem = sentinel.prev.item;
        sentinel.prev.prev.next=sentinel;
        sentinel.prev = sentinel.prev.prev;
        size--;
        return lastItem;
    }
    // Gets the item at the given index, where 0 is the front,
    // 1 is the next item, and so forth. If no such item
    // exists, returns null.
    public T get(int index){
        if(index > size - 1) {
            return null;
        }
        Node n = sentinel.next;
        for(int i = 0; i < index; i++) {
            n = n.next;
        }
        return n.item;
    }
    public T getRecursive(int index) {
        if(index > size - 1) {
            return null;
        }

        return recurisiveHelper(sentinel.next, index);
    }
    private T recurisiveHelper(Node n, int rest) {
        if (rest == 0) {
            return n.item;
        }

        return recurisiveHelper(n.next, rest - 1);
    }

    // The Deque objects we’ll make are iterable (i.e. Iterable<T>)
    // so we must provide this method to return an iterator.
    public Iterator<T> iterator(){
        return new DequeIterator<>(this);
    }

    // Returns whether or not the parameter o is equal to the Deque.
    // o is considered equal if it is a Deque and if it contains the
    // same contents (as goverened by the generic T’s equals method)
    // in the same order.


    public boolean equals(Object o){
        if (! (o instanceof LinkedListDeque)){
            return false;
        }

        int j = 0;
        for(Object i : (LinkedListDeque)o){
            if(j >= size || (T)i != get(j)){
                return false;
            }
            j++;
        }
        return true;
    }
}