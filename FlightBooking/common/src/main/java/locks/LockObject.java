package locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockObject<E> {

    private final Lock readLock;
    private final Lock writeLock;
    private final E elem;

    public LockObject(E elem) {
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        this.readLock = rw.readLock();
        this.writeLock = rw.writeLock();
        this.elem = elem;
    }

    public void readLock() {
        readLock.lock();
    }

    public void readUnlock() {
        readLock.unlock();
    }

    public void writeLock() {
        writeLock.lock();
    }

    public void writeUnlock() {
        writeLock.unlock();
    }

    public E elem() {
        return this.elem;
    }
}
