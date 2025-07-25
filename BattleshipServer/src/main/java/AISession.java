import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AISession implements Runnable {
    private final Server parentServer;
    private final Socket playerSocket;
    private ObjectOutputStream playerOut;
    private ObjectInputStream playerIn;
    private final Object turnLock = new Object();
    private Character[][] playerGrid = new Character[10][10];
    private Character[][] aiGrid = new Character[10][10];
    int playerHealth = 17;
    int aiHealth = 17;
    Integer[] lengths = {5, 4, 3, 3, 2};
    Character[] shipSym = {'C', 'B', 'c', 'S', 'D'}; // Big C for carrier, lil c for cruiser
    Character[] hOrV = {'h', 'v'};

    public AISession(Socket playerSocket, ObjectOutputStream pOut, ObjectInputStream pIn, Server server) {
        initGrid();
        placeAIShips();

        parentServer = server;

        this.playerSocket = playerSocket;

        try {
            // Initialize output stream
            playerOut = pOut;

            // Initialize input stream
            playerIn = pIn;

            // Inform both players that they have been matched
            playerOut.writeObject("MATCHED!");
        } catch (IOException e) {
            System.err.println("Error setting up AI session: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Get grid
            playerGrid = (Character[][]) playerIn.readObject();
            //printBoards();

            // Main game loop
            while (true) {
                // Player's turn
                Message p1Turn = new Message(0, true);
                p1Turn.outGrid = aiGrid;

                playerOut.writeObject(p1Turn);
                synchronized (turnLock) {
                    processPlayerTurn();
                }

                // Check if Player has won
                if (aiHealth == 0) {
                    synchronized (turnLock) {
                        playerOut.writeObject(new Message(2));
                    }
                    break; // Game ends
                }

                playerOut.writeObject(new Message(0, false));
                // AI's turn
                synchronized (turnLock) {
                    processAITurn();
                }

                // Check if AI has won
                if (playerHealth == 0) {
                    synchronized (turnLock) {
                        playerOut.writeObject(new Message(3));
                    }
                    break; // Game ends
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during game session: " + e.getMessage());
        } finally {
            // Close sockets and inform server that game session has ended
            try {
                parentServer.removeAISession(this);

                if (playerOut != null) {
                    playerOut.close();
                }
                if (playerIn != null) {
                    playerIn.close();
                }
                if (playerSocket != null) {
                    playerSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing sockets or streams: " + e.getMessage());
            }
        }
    }

    private void processAITurn() throws IOException, ClassNotFoundException {
        Random randGen = new Random();

        int inX = randGen.nextInt(10);;
        int inY = randGen.nextInt(10);;

        int critNum = randGen.nextInt(10);

        if (critNum == 0 || critNum == 1) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (playerGrid[i][j] != 'E' && playerGrid[i][j] != 'X' && playerGrid[i][j] != 'O') {
                        inX = j;
                        inY = i;
                    }
                }
            }
        }
        else {
            while (playerGrid[inY][inX] == 'X' || playerGrid[inY][inX] == 'O') {
                inX = randGen.nextInt(10);
                inY = randGen.nextInt(10);
            }
        }

        int[] outCoords = {inX, inY};

        try {
            Thread.sleep(1000);
        }
        catch (Exception e) {

        }

        if (playerGrid[inY][inX] != 'E') { // Hit
            playerGrid[inY][inX] = 'X';
            playerHealth--;

            playerOut.writeObject(new Message(1, outCoords, true));
        }
        else { // Miss
            playerGrid[inY][inX] = 'O';

            playerOut.writeObject(new Message(1, outCoords, false));
        }

        try {
            Thread.sleep(1500);
        }
        catch (Exception e) {

        }
    }

    private void processPlayerTurn() throws IOException, ClassNotFoundException {
        Message inMsg = (Message) playerIn.readObject();
        int[] inCoords = inMsg.attackCoords;
        int inX = inCoords[0];
        int inY = inCoords[1];

        if (aiGrid[inY][inX] != 'E') { // Hit
            aiGrid[inY][inX] = 'X';
            aiHealth--;
        }
        else { // Miss
            aiGrid[inY][inX] = 'O';
        }

        try {
            Thread.sleep(1500);
        }
        catch (Exception e) {

        }
    }

    public void initGrid() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                aiGrid[i][j] = 'E'; // E for empty
            }
        }
    }

    public boolean validatePlacement(int x, int y, char dir, int length) {
        if (dir == 'v') {
            if (y + length > 10) {
                return false;
            }

            for (int i = y; i < length + y; i++) {
                if (aiGrid[i][x] != 'E') {
                    return false;
                }
            }
        }
        else if (dir == 'h') {
            if (x + length > 10) {
                return false;
            }

            for (int i = x; i < length + x; i++) {
                if (aiGrid[y][i] != 'E') {
                    return false;
                }
            }
        }

        return true;
    }

    public void placeShip(int x, int y, char dir, int length, char sym) {
        if (dir == 'v') {
            for (int i = y; i < length + y; i++) {
                aiGrid[i][x] = sym;
            }
        }
        else if (dir == 'h') {
            for (int i = x; i < length + x; i++) {
                aiGrid[y][i] = sym;
            }
        }
    }

    public void placeAIShips() {
        Random randGen = new Random();

        for (int i = 0; i < 5; i++) {
            int x = randGen.nextInt(10);
            int y = randGen.nextInt(10);
            int whichDir = randGen.nextInt(2);

            while (validatePlacement(x, y, hOrV[whichDir], lengths[i]) == false) {
                x = randGen.nextInt(10);
                y = randGen.nextInt(10);
                whichDir = randGen.nextInt(2);
            }

            placeShip(x, y, hOrV[whichDir], lengths[i], shipSym[i]);
        }
    }
}
