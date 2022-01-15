import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
