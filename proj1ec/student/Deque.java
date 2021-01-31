package student;

/**
 * Created by hug on 2/4/2017.
 * Added isEmpty() default implementation on 2/3/2019.
 */
public interface Deque<Item> {
    void addFirst(Item x);
    void addLast(Item x);

    default boolean isEmpty() {
        return size() == 0;
    }

    int size();
    void printDeque();
    Item removeFirst();
    Item removeLast();
    Item get(int index);
}