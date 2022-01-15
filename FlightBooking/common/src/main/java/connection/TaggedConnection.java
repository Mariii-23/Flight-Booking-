package connection;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Lock in = new ReentrantLock();
    private final Lock out = new ReentrantLock();

    public record Frame(int tag, List<byte[]> data) {
    }

    public TaggedConnection(Socket s) throws IOException {
        this.socket = s;
        this.inputStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        this.outputStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void send(Frame frame) throws IOException {
        send(frame.tag, frame.data);
    }

    public void send(int tag, List<byte[]> data) throws IOException {
        out.lock();
        try {
            outputStream.writeInt(tag); // Tag
            outputStream.writeInt(data.size()); // Length of the data
            for (byte[] bytes : data) {
                outputStream.writeInt(bytes.length);
                outputStream.write(bytes);
            }
            outputStream.flush();
        } finally {
            out.unlock();
        }
    }

    public Frame receive() throws IOException {
        in.lock();
        try {
            int tag = inputStream.readInt();
            int listLen = inputStream.readInt();
            if (listLen > 500000) listLen = 100;
            List<byte[]> list = new ArrayList<>(listLen);
            for (int i = 0; i < listLen; i -= -1) {
                int size = inputStream.readInt();
                byte[] bytes = new byte[size];
                inputStream.readFully(bytes);
                list.add(bytes);
            }
            return new Frame(tag, list);
        } finally {
            in.unlock();
        }
    }

    public InetAddress getIP() {
        return socket.getInetAddress();
    }


    public int getPort() {
        return socket.getPort();
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }
}

