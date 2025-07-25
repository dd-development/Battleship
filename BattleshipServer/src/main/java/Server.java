import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Server {
    private ArrayList<Socket> matchQueue = new ArrayList<>();
    private final ArrayList<GameSession> gameSessions;
    private final ArrayList<AISession> aiSessions = new ArrayList<>();

    public Server() {
        gameSessions = new ArrayList<>();
    }

    // Method to start the server and listen for client connections
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {
            // Create a server socket on port 5555
            //serverSocket = new ServerSocket(5555);
            System.out.println("Server started. Waiting for clients to connect...");

            // Listen for client connections indefinitely
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket checkSocket = serverSocket.accept();
                ObjectInputStream checkIn = new ObjectInputStream(checkSocket.getInputStream());
                String checkGT = "";
                try{checkGT = (String) checkIn.readObject();}catch(Exception e){}
                checkSocket.close();

                // Accept a new client connection
                Socket playerSocket = serverSocket.accept();
                System.out.println("New client connected: " + playerSocket);
                try {
                    if (checkGT.equals("HUMAN")) {
                        matchQueue.add(playerSocket);
                        System.out.println(matchQueue.size());

                        if (matchQueue.size() == 2) {

                            // Create a new game session with the connected client
                            ObjectOutputStream p1O = new ObjectOutputStream(matchQueue.get(0).getOutputStream());
                            ObjectOutputStream p2O = new ObjectOutputStream(matchQueue.get(1).getOutputStream());

                            ObjectInputStream p1I = new ObjectInputStream(matchQueue.get(0).getInputStream());
                            ObjectInputStream P2I = new ObjectInputStream(matchQueue.get(1).getInputStream());

                            GameSession newGameSession = new GameSession(matchQueue.get(0), matchQueue.get(1), p1O, p2O, p1I, P2I, this);

                            // Add the game session to the list of active sessions
                            gameSessions.add(newGameSession);

                            // Start the game session in a new thread
                            Thread gameSessionThread = new Thread(newGameSession);
                            gameSessionThread.start();

                            matchQueue.clear();
                        }
                    }
                    else {
                        System.out.println("AI Game Incoming...");

                        ObjectOutputStream pO = new ObjectOutputStream(playerSocket.getOutputStream());
                        ObjectInputStream pI = new ObjectInputStream(playerSocket.getInputStream());

                        AISession newAISession = new AISession(playerSocket, pO, pI, this);
                        aiSessions.add(newAISession);

                        Thread aiSessionThread = new Thread(newAISession);
                        aiSessionThread.start();
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    // Method to remove a game session (e.g., when a game ends)
    public void removeGameSession(GameSession gameSession) {
        gameSessions.remove(gameSession);
    }

    // Method to remove AI session when game ends
    public void removeAISession(AISession aiSession) {
        aiSessions.remove(aiSession);
    }

    // Getter for accessing the list of game sessions
    public List<GameSession> getGameSessions() {
        return gameSessions;
    }

    // Getter for accessing the list of AI sessions
    public List<AISession> getAISessions() {
        return aiSessions;
    }

    // Main method to start the server
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}