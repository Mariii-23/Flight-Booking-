package airport;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PossiblePath {
    private final String thisCity;
    private final boolean isDestiny;
    private final List<PossiblePath> connections;


    public PossiblePath(boolean isDest, String from) {
        thisCity = from;

        this.isDestiny = isDest;
        connections = new LinkedList<>();
    }

    public static PossiblePath deserialize(byte[] bytes) {
        return deserializeAux(ByteBuffer.wrap(bytes));
    }

    private static PossiblePath deserializeAux(ByteBuffer bb) {
        int sizeNameCity = bb.getInt();
        byte[] thisCityBytes = new byte[sizeNameCity];
        bb.get(thisCityBytes);
        String thisCityDes = new String(thisCityBytes);
        int size = bb.getInt();
        PossiblePath possiblePath = new PossiblePath(size == 0, thisCityDes);

        List<PossiblePath> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(deserializeAux(bb));
        }
        for (PossiblePath path : list)
            possiblePath.addPossiblePath(path);
        return possiblePath;
    }

    public void addPossiblePath(PossiblePath toInsert) {
        connections.add(toInsert);
    }

    public int numPossiblePaths() {
        return connections.size();
    }

    public byte[] serialize() {
        byte[] cityToByte = thisCity.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + cityToByte.length + Integer.BYTES);

        bb.putInt(cityToByte.length);
        bb.put(cityToByte);

        bb.putInt(connections.size());

        for (PossiblePath possiblePath : connections) {
            byte[] elem = possiblePath.serialize();
            ByteBuffer newBuffer = ByteBuffer.allocate(bb.capacity() + elem.length);
            newBuffer.put(bb.array());
            newBuffer.put(elem);

            bb = newBuffer;
        }
        return bb.array();
    }

    @Override
    public String toString() {
        if (isDestiny) return " [Destination " + thisCity + "] ";
        StringBuilder res = new StringBuilder("airport.PossiblePath{ here: " + thisCity);
        for (PossiblePath connection : connections) {
            res.append("\n  {").append(connection.toString()).append("}");
        }
        return res.toString();
    }

    public String toStringPretty(String start) {
        if (isDestiny) return start + " [" + thisCity + "]\n ";

        String header = start + " " + thisCity;
        StringBuilder res = new StringBuilder();

        for (PossiblePath connection : connections) {
            res.append(connection.toStringPretty(header));
        }
        return res.toString();
    }
}
