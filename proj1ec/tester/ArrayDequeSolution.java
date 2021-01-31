package tester;

import java.util.LinkedList;

/**
 * Isn't this solution kinda... cheating? Yes.
 * The aesthete will be especially alarmed by the fact that this
 * supposed ArrayDeque is actually using a LinkedList. SAD!
 */
public class ArrayDequeSolution<Item> extends LinkedList<Item> {
    public void printDeque() {
        System.out.println("dummy");
    }

    public Item getRecursive(int i) {
        return get(i);
    }

    public Item removeFirst() {
        try {
            return super.removeFirst();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Item removeLast() {
        try {
            return super.removeLast();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
