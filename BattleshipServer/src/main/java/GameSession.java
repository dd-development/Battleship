import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class GameSession implements Runnable {
    private final Server parentServer;
    private final Socket player1Socket;
    private final Socket player2Socket;
    private ObjectOutputStream player1Out;
    private ObjectOutputStream player2Out;
    private ObjectInputStream player1In;
    private ObjectInputStream player2In;
    private final Object turnLock = new Object();
    private Character[][] p1Grid = new Character[10][10];
    private Character[][] p2Grid = new Character[10][10];
    int p1Health = 17;
    int p2Health = 17;
    public GameSession(Socket player1Socket, Socket player2Socket, ObjectOutputStream p1O, ObjectOutputStream p2O, ObjectInputStream p1I, ObjectInputStream p2I, Server server) {
        parentServer = server;

        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;

        try {
            // Initialize output streams for both players
            player1Out = p1O;
            player2Out = p2O;

            // Initialize input streams for both players
            player1In = p1I;
            player2In = p2I;

            // Inform both players that they have been matched
            player1Out.writeObject("MATCHED!");
            player2Out.writeObject("MATCHED!");
        } catch (IOException e) {
            System.err.println("Error setting up game session: " + e.getMessage());
        }
    }

    public void printBoards() { // USED FOR DEBUGGING PURPOSES
        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.toString(p1Grid[i]));
        }
        System.out.println("\n");

        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.toString(p2Grid[i]));
        }
    }

    private void processPlayerTurn(ObjectInputStream playerIn, ObjectOutputStream otherPlayerOut, int player) throws IOException, ClassNotFoundException {
        Message inMsg = (Message) playerIn.readObject();
        int[] inCoords = inMsg.attackCoords;
        int inX = inCoords[0];
        int inY = inCoords[1];

        if (player == 1) {
            if (p2Grid[inY][inX] != 'E') { // Hit
                p2Grid[inY][inX] = 'X';
                p2Health--;

                otherPlayerOut.writeObject(new Message(1, inCoords, true));
            }
            else { // Miss
                p2Grid[inY][inX] = 'O';
                otherPlayerOut.writeObject(new Message(1, inCoords, false));
            }
        }
        else {
            if (p1Grid[inY][inX] != 'E') { // Hit
                p1Grid[inY][inX] = 'X';
                p1Health--;

                otherPlayerOut.writeObject(new Message(1, inCoords, true));
            }
            else { // Miss
                p1Grid[inY][inX] = 'O';
                otherPlayerOut.writeObject(new Message(1, inCoords, false));
            }
        }

        try {
            Thread.sleep(1500);
        }
        catch (Exception e) {

        }
    }

    @Override
    public void run() {
        try {
            // Get grids
            p1Grid = (Character[][]) player1In.readObject();

            p2Grid = (Character[][]) player2In.readObject();
            //printBoards();

            // Main game loop
            while (true) {
                // Player 1's turn


                player2Out.writeObject(new Message(0, false));
                Message p1Turn = new Message(0, true);
                p1Turn.outGrid = p2Grid;

//                for (int i = 0; i < 10; i++) {
//                    System.out.println(Arrays.toString(p1Turn.outGrid[i]));
//                }

                player1Out.writeObject(p1Turn);
                synchronized (turnLock) {
                    processPlayerTurn(player1In, player2Out, 1);
                }

                // Check if Player 1 has won
                if (p2Health == 0) {
                    synchronized (turnLock) {
                        player1Out.writeObject(new Message(2));
                        player2Out.writeObject(new Message(3));
                    }
                    break; // Game ends
                }

                player1Out.writeObject(new Message(0, false));
                Message p2Turn = new Message(0, true);
                p2Turn.outGrid = p1Grid;
                player2Out.writeObject(p2Turn);
                // Player 2's turn
                synchronized (turnLock) {
                    processPlayerTurn(player2In, player1Out, 2);
                }

                // Check if Player 2 has won
                if (p1Health == 0) {
                    synchronized (turnLock) {
                        player2Out.writeObject(new Message(2));
                        player1Out.writeObject(new Message(3));
                    }
                    break; // Game ends
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during game session: " + e.getMessage());
        } finally {
            // Close sockets and inform server that game session has ended
            try {
                parentServer.removeGameSession(this);

                if (player1Out != null) {
                    player1Out.close();
                }
                if (player2Out != null) {
                    player2Out.close();
                }
                if (player1In != null) {
                    player1In.close();
                }
                if (player2In != null) {
                    player2In.close();
                }
                if (player1Socket != null) {
                    player1Socket.close();
                }
                if (player2Socket != null) {
                    player2Socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing sockets or streams: " + e.getMessage());
            }
        }
    }
}