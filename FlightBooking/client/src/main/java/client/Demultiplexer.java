package client;

import connection.TaggedConnection;
import connection.TaggedConnection.Frame;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer {
    private final TaggedConnection conn;
    private final ReentrantLock l = new ReentrantLock();
    private final Map<Integer, FrameValue> map = new HashMap<>();
    private IOException exception = null;

    public Demultiplexer(TaggedConnection conn) {
        this.conn = conn;
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    Frame frame = conn.receive();
                    l.lock();
                    try {
                        FrameValue fv = map.get(frame.tag());
                        if (fv == null) {
                            fv = new FrameValue();
                            map.put(frame.tag(), fv);
                        }
                        fv.queue.add(frame.data());
                        fv.c.signal();
                    } finally {
                        l.unlock();
                    }
                }
            } catch (IOException e) {
                exception = e;
            }
        }).start();
    }

    public void send(int tag, List<byte[]> data) throws IOException {
        conn.send(tag, data);
    }

    public List<byte[]> receive(int tag) throws IOException, InterruptedException {
        l.lock();
        FrameValue fv;
        try {
            fv = map.get(tag);
            if (fv == null) {
                fv = new FrameValue();
                map.put(tag, fv);
            }
            fv.waiters++;
            while (true) {
                if (!fv.queue.isEmpty()) {
                    fv.waiters--;
                    List<byte[]> reply = fv.queue.poll();
                    if (fv.waiters == 0 && fv.queue.isEmpty())
                        map.remove(tag);
                    return reply;
                }
                if (exception != null) {
                    throw exception;
                }
                fv.c.await();
            }
        } finally {
            l.unlock();
        }
    }

    public void close() throws IOException {
        conn.close();
    }

    private class FrameValue {
        int waiters = 0;
        Queue<List<byte[]>> queue = new ArrayDeque<>();
        Condition c = l.newCondition();

        public FrameValue() {

        }
    }

}
