package utils;

import java.util.ArrayList;
import java.util.List;

public class CircularStack<T> {

    private List<T> array;
    private int N;

    public CircularStack(int N) {
        this.N = N;
        array = new ArrayList<T>(N);
    }

    public void add(T newObject) {
        if (array.size() < N) {
            array.add(newObject);
            return;
        }

        array.remove(0);
        array.add(newObject);
    }

    public T pop() {
        if (this.array.size() == 0) {
            return null;
        }
        return array.remove(array.size() - 1);
    }

    public T next() {
        if (this.array.size() == 0) {
            return null;
        }
        return array.get(array.size() - 1);
    }

    public void clear() {
        this.array.clear();
    }

    public boolean isEmpty() {
        return array.size() == 0;
    }
}
