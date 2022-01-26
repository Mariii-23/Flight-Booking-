package server;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<T> {
    private final Queue<T> queue = new ArrayDeque<>();
    private final int limit;

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BlockingQueue(int limit) {
        this.limit = limit;
    }

    public void put(T t) {
        lock.lock();
        try {
            while (isFull()) {
                try {
                    notFull.await();
                } catch (InterruptedException ignored) {
                }
            }
            queue.add(t);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        T t;
        lock.lock();
        try {
            while (isEmpty()) {
                notEmpty.await();
            }
            t = queue.poll();
            notFull.signal();
        } finally {
            lock.unlock();
        }
        return t;
    }

    private boolean isEmpty() {
        return queue.size() == 0;
    }

    private boolean isFull() {
        return queue.size() == limit;
    }

}
