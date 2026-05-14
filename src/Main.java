import server.VoteWiseServer;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Starting VoteWise Server...");
            VoteWiseServer server = new VoteWiseServer();
            server.startServer();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
