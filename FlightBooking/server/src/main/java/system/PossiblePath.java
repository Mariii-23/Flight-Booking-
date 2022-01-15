package system;

import java.util.LinkedList;
import java.util.List;

public class PossiblePath {
    private String thisCity;
    private boolean isDestiny;
    private List<PossiblePath> connections;


    public PossiblePath(boolean isDest, String from) {
        thisCity = from;

        if (isDest)
            isDestiny = true;
        else{
            isDestiny = false;
            connections = new LinkedList<>();
        }
    }

    public void addPossiblePath(PossiblePath toInsert){
        connections.add(toInsert);
    }

    public int numPossiblePaths(){
        return connections.size();
    }

    @Override
    public String toString() {
        if (isDestiny) return " [Destination " + thisCity + "] ";
        StringBuilder res = new StringBuilder( "system.PossiblePath{ here: " + thisCity );
        for (PossiblePath connection : connections){
            res.append("\n  {" + connection.toString() + "}");
        }
        return res.toString();
    }

    public String toStringPretty(String start){
        if (isDestiny) return start + " [" + thisCity + "]\n ";

        String header = start + " " + thisCity;
        StringBuilder res = new StringBuilder();

        for (PossiblePath connection : connections){
             res.append(connection.toStringPretty(header));
        }
        return res.toString();
    }
    }
