
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import javafx.scene.input.MouseEvent;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class GUIClient extends Application{
    Character[][] guiGrid = new Character[10][10];
    final int[] placedPiece = {0};
    Integer[] lengths = {5, 4, 3, 3, 2};
    Character[] shipSym = {'C', 'B', 'c', 'S', 'D'}; // Big C for carrier, lil c for cruiser
    String[] fileNamesV = {"carrier_v.png", "battle_v.png", "cruiser_v.png", "sub_v.png", "destroyer_v.png"};
    String[] fileNamesH = {"carrier_h.png", "battle_h.png", "cruiser_h.png", "sub_h.png", "destroyer_h.png"};
    String[] labelNames = {"CARRIER (5 PC)", "BATTLESHIP (4 PC)", "CRUISER (3 PC)", "SUB (3 PC)", "DESTROYER (2 PC)"};
    char placeMode = 'v'; // VERTICAL PLACEMENT BY DEFAULT
    Client clientConnection;
    boolean gameStarted = false;
    boolean attackCompleted = false;
    Character[][] enemyGrid = new Character[10][10];
    boolean musicPlaying = true;
    boolean firstTurn = true;
    String gameType = "HUMAN";

    public void initGrid() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                guiGrid[i][j] = 'E'; // E for empty
            }
        }
    }

    public boolean validatePlacement(int x, int y, char dir, int length) {
        if (dir == 'v') {
            if (y + length > 10) {
                return false;
            }

            for (int i = y; i < length + y; i++) {
                if (guiGrid[i][x] != 'E') {
                    return false;
                }
            }
        }
        else if (dir == 'h') {
            if (x + length > 10) {
                return false;
            }

            for (int i = x; i < length + x; i++) {
                if (guiGrid[y][i] != 'E') {
                    return false;
                }
            }
        }

        return true;
    }

    public void placeShip(int x, int y, char dir, int length, char sym) {
        if (dir == 'v') {
            for (int i = y; i < length + y; i++) {
                guiGrid[i][x] = sym;
            }
        }
        else if (dir == 'h') {
            for (int i = x; i < length + x; i++) {
                guiGrid[y][i] = sym;
            }
        }
    }

    public void printBoard() { // USED FOR DEBUGGING PURPOSES
        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.toString(guiGrid[i]));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        initGrid();

        File video = new File("src/main/resources/b.mp4");
        Media bgVid = new Media(video.toURI().toURL().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(bgVid);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setAutoPlay(true);
        MediaView mediaView = new MediaView(mediaPlayer);

        File musicFile = new File("src/main/resources/music.mp3");
        Media music = new Media(musicFile.toURI().toURL().toString());
        MediaPlayer musicPlayer = new MediaPlayer(music);
        musicPlayer.setVolume(.1);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setAutoPlay(true);
        MediaView musicView = new MediaView(musicPlayer);

        File explosionFile = new File("src/main/resources/explosion.mp3");
        AudioClip explosion = new AudioClip(explosionFile.toURI().toURL().toString());
        explosion.setVolume(.1);

        File splashFile = new File("src/main/resources/splash.mp3");
        AudioClip splash = new AudioClip(splashFile.toURI().toURL().toString());
        splash.setVolume(.1);

        File winFile = new File("src/main/resources/win.mp3");
        AudioClip win = new AudioClip(winFile.toURI().toURL().toString());
        win.setVolume(.1);

        File loseFile = new File("src/main/resources/lose.mp3");
        AudioClip lose = new AudioClip(loseFile.toURI().toURL().toString());
        lose.setVolume(.1);

        Image logo = new Image("logo.png");
        ImageView logoView = new ImageView(logo);

        Label instructions = new Label("PLACE YOUR CARRIER (5 PC)");
        instructions.setTextFill(Color.RED);
        instructions.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 48));

        Label gameTypeLabel = new Label("PLAY AGAINST HUMAN OR AI?");
        gameTypeLabel.setTextFill(Color.WHITE);
        gameTypeLabel.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 48));

        Button vB = new Button("Vertical");
        Button hB = new Button("Horizontal");
        Button rB = new Button("Reset");
        Button cB = new Button("Continue");
        Button mB = new Button("Toggle Audio");
        Button mB2 = new Button("Toggle Audio");
        Button humanB = new Button("Human");
        Button aiB = new Button("AI");

        vB.setPrefSize(100, 50);
        vB.setTextFill(Color.BLACK);
        vB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        vB.setStyle("-fx-background-color: red");
        vB.setDisable(true);

        hB.setPrefSize(130, 50);
        hB.setTextFill(Color.BLACK);
        hB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        hB.setStyle("-fx-background-color: red");

        rB.setPrefSize(100, 50);
        rB.setTextFill(Color.WHITE);
        rB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        rB.setStyle("-fx-background-color: blue");

        cB.setPrefSize(130, 50);
        cB.setTextFill(Color.WHITE);
        cB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        cB.setStyle("-fx-background-color: blue");
        cB.setDisable(true);

        mB.setPrefSize(150, 50);
        mB.setTextFill(Color.BLACK);
        mB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        mB.setStyle("-fx-background-color: orange");

        mB2.setPrefSize(150, 50);
        mB2.setTextFill(Color.BLACK);
        mB2.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        mB2.setStyle("-fx-background-color: orange");

        humanB.setPrefSize(150, 50);
        humanB.setTextFill(Color.BLACK);
        humanB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        humanB.setStyle("-fx-background-color: white");
        humanB.setDisable(true);

        aiB.setPrefSize(150, 50);
        aiB.setTextFill(Color.BLACK);
        aiB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        aiB.setStyle("-fx-background-color: white");

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(musicView, mediaView, logoView, instructions, vB, hB, rB, cB, mB, gameTypeLabel, humanB, aiB);

        stackPane.setAlignment(mediaView, Pos.TOP_LEFT);
        stackPane.setAlignment(logoView, Pos.TOP_RIGHT);
        stackPane.setAlignment(instructions, Pos.TOP_RIGHT);
        stackPane.setAlignment(vB, Pos.TOP_RIGHT);
        stackPane.setAlignment(hB, Pos.TOP_RIGHT);
        stackPane.setAlignment(rB, Pos.TOP_RIGHT);
        stackPane.setAlignment(cB, Pos.TOP_RIGHT);
        stackPane.setAlignment(mB, Pos.TOP_RIGHT);

        stackPane.setAlignment(gameTypeLabel, Pos.TOP_RIGHT);
        stackPane.setAlignment(humanB, Pos.TOP_RIGHT);
        stackPane.setAlignment(aiB, Pos.TOP_RIGHT);

        instructions.setTranslateX(-20);
        instructions.setTranslateY(150);

        gameTypeLabel.setTranslateX(-20);
        gameTypeLabel.setTranslateY(600);

        vB.setTranslateX(-350);
        vB.setTranslateY(250);

        hB.setTranslateX(-150);
        hB.setTranslateY(250);

        rB.setTranslateX(-350);
        rB.setTranslateY(350);

        cB.setTranslateX(-150);
        cB.setTranslateY(350);

        mB.setTranslateX(-225);
        mB.setTranslateY(450);

        mB2.setTranslateX(-225);
        mB2.setTranslateY(450);

        humanB.setTranslateX(-320);
        humanB.setTranslateY(700);

        aiB.setTranslateX(-120);
        aiB.setTranslateY(700);

        hB.setOnAction(e->{
            placeMode = 'h';
            vB.setDisable(false);
            hB.setDisable(true);
        });

        vB.setOnAction(e->{
            placeMode = 'v';
            hB.setDisable(false);
            vB.setDisable(true);
        });

        rB.setOnAction(e->{
            cB.setDisable(true);
            instructions.setText("PLACE YOUR CARRIER (5 PC)");

            for (int i = 0; i < placedPiece[0]; i++) {
                stackPane.getChildren().remove(stackPane.getChildren().size()-1);
            }

            initGrid();
            placedPiece[0] = 0;
        });

        mB.setOnAction(e->{
            if (musicPlaying == true) {
                musicPlaying = false;
                musicPlayer.pause();
                splash.setVolume(0);
                explosion.setVolume(0);
                win.setVolume(0);
                lose.setVolume(0);
            }
            else {
                musicPlaying = true;
                musicPlayer.play();
                splash.setVolume(.1);
                explosion.setVolume(.1);
                win.setVolume(.1);
                lose.setVolume(.1);
            }
        });

        mB2.setOnAction(e->{
            if (musicPlaying == true) {
                musicPlaying = false;
                musicPlayer.pause();
                splash.setVolume(0);
                explosion.setVolume(0);
                win.setVolume(0);
                lose.setVolume(0);
            }
            else {
                musicPlaying = true;
                musicPlayer.play();
                splash.setVolume(.1);
                explosion.setVolume(.1);
                win.setVolume(.1);
                lose.setVolume(.1);
            }
        });

        humanB.setOnAction(e->{
            gameType = "HUMAN";
            aiB.setDisable(false);
            humanB.setDisable(true);
        });

        aiB.setOnAction(e->{
            gameType = "AI";
            humanB.setDisable(false);
            aiB.setDisable(true);
        });

        MediaView mediaViewAttack = new MediaView(mediaPlayer);
        //MediaView mediaViewDefend = new MediaView(mediaPlayer);

        StackPane attackPane = new StackPane();
        //StackPane defendPane = new StackPane();

        attackPane.setAlignment(mediaViewAttack, Pos.TOP_LEFT);
        //defendPane.setAlignment(mediaViewDefend, Pos.TOP_LEFT);

        Label aLabel = new Label("YOUR TURN");
        aLabel.setTextFill(Color.RED);
        aLabel.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 72));

        Label dLabel = new Label("ENEMY TURN");
        dLabel.setTextFill(Color.RED);
        dLabel.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 72));

        attackPane.getChildren().addAll(mediaViewAttack, aLabel, mB2);
        //defendPane.getChildren().addAll(mediaViewDefend, dLabel);

        attackPane.setAlignment(aLabel, Pos.TOP_RIGHT);
        attackPane.setAlignment(mB2, Pos.TOP_RIGHT);
        //defendPane.setAlignment(dLabel, Pos.TOP_RIGHT);

        attackPane.setStyle("-fx-background-color: black");
        //defendPane.setStyle("-fx-background-color: black");

        Scene attackScene = new Scene(attackPane, 1600, 1000);
        //Scene defendScene = new Scene(defendPane, 1600, 1000);

        Scene mainScene = new Scene(stackPane, 1600, 1000);

        StackPane endPane = new StackPane();
        endPane.setStyle("-fx-background-color: black");

        Label endLabel = new Label();
        endLabel.setTextFill(Color.RED);
        endLabel.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 144));

        Button pB = new Button("Play Again");
        Button eB = new Button("Exit");

        pB.setPrefSize(150, 50);
        pB.setTextFill(Color.BLACK);
        pB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        pB.setStyle("-fx-background-color: red");

        eB.setPrefSize(150, 50);
        eB.setTextFill(Color.BLACK);
        eB.setFont(Font.font("Impact", FontWeight.EXTRA_BOLD, 20));
        eB.setStyle("-fx-background-color: red");

        endPane.setAlignment(endLabel, Pos.CENTER);
        endPane.setAlignment(endLabel, Pos.CENTER);
        endPane.setAlignment(endLabel, Pos.CENTER);

        pB.setTranslateX(100);
        pB.setTranslateY(300);

        eB.setTranslateX(-100);
        eB.setTranslateY(300);

        pB.setOnAction(e->{
            try{clientConnection.socketClient.close();}catch(Exception caughtE){}
            clientConnection = null;

            initGrid();
            placedPiece[0] = 0;
            placeMode = 'v'; // VERTICAL PLACEMENT BY DEFAULT
            gameStarted = false;
            attackCompleted = false;
            enemyGrid = new Character[10][10];
            firstTurn = true;
            gameType = "HUMAN";

            instructions.setText("PLACE YOUR CARRIER (5 PC)");
            vB.setDisable(true);
            hB.setDisable(false);
            rB.setDisable(false);
            cB.setDisable(true);
            humanB.setDisable(true);
            aiB.setDisable(false);

            stackPane.getChildren().clear();
            stackPane.getChildren().addAll(musicView, mediaView, logoView, instructions, vB, hB, rB, cB, mB, gameTypeLabel, humanB, aiB);

            attackPane.getChildren().clear();
            attackPane.getChildren().addAll(mediaViewAttack, aLabel, mB2);

            primaryStage.setScene(mainScene);
            primaryStage.show();

            if (musicPlaying == true) {
                musicPlayer.play();
            }
        });

        eB.setOnAction(e->{
            System.exit(0);
        });

        endPane.getChildren().addAll(endLabel, pB, eB);

        Scene endScene = new Scene(endPane, 1600, 1000);

        cB.setOnAction(e->{
            // CALLBACK FUNCTION HERE --------------------------------------------------------------------------------
            clientConnection = new Client(data->{
                Message inMsg = (Message) data;

                if (gameStarted == false) {
                    gameStarted = true;
                }

                if (inMsg.protocol == 0) { // PROCESSES TURN
                    if (inMsg.yourTurn == true) { // YOUR TURN
                        if (firstTurn == true) {
                            firstTurn = false;
                            enemyGrid = inMsg.outGrid;
                        }

                        attackCompleted = false;
                        Platform.runLater(()->{
                            primaryStage.setScene(attackScene);
                            primaryStage.show();
                        });
                    }
                    else {
                        Platform.runLater(()->{ // NOT YOUR TURN
                            primaryStage.setScene(mainScene);
                            primaryStage.show();
                        });
                    }
                }
                else if (inMsg.protocol == 1) {
                    Platform.runLater(()->{
                        int iX = inMsg.attackCoords[0];
                        int iY = inMsg.attackCoords[1];

                        String imgDir = "";
                        if (inMsg.isHit == true) {
                            imgDir = "hit.png";
                            explosion.play();
                        }
                        else {
                            imgDir = "miss.png";
                            splash.play();
                        }
                        Image hitOrMissImg = new Image(imgDir);
                        ImageView hitOrMissView = new ImageView(hitOrMissImg);

                        stackPane.getChildren().add(hitOrMissView);
                        stackPane.setAlignment(hitOrMissView, Pos.TOP_LEFT);
                        hitOrMissView.setTranslateX(((iX * 90) + 99));
                        hitOrMissView.setTranslateY(((iY * 90) + 99));
                    });
                }
                else if (inMsg.protocol == 2) { // WIN
                    Platform.runLater(()->{ // NOT YOUR TURN
                        musicPlayer.stop();

                        win.play();

                        endLabel.setText("YOU WIN!");
                        primaryStage.setScene(endScene);
                        primaryStage.show();
                    });
                }
                else if (inMsg.protocol == 3) { // LOSS
                    Platform.runLater(()->{ // NOT YOUR TURN
                        musicPlayer.stop();
                        lose.play();

                        endLabel.setText("YOU LOSE");
                        primaryStage.setScene(endScene);
                        primaryStage.show();
                    });
                }
            });
            // END CALLBACK FUNCTION --------------------------------------------------------------------------------

            try {
                Socket checkSocket = new Socket("127.0.0.1",5555);
                ObjectOutputStream checkOut = new ObjectOutputStream(checkSocket.getOutputStream());
                checkSocket.setTcpNoDelay(true);

                checkOut.writeObject(gameType);
                checkSocket.close();
            }
            catch(Exception caughtE) {}

            clientConnection.start();

            vB.setDisable(true);
            hB.setDisable(true);
            rB.setDisable(true);
            cB.setDisable(true);
            humanB.setDisable(true);
            aiB.setDisable(true);
            String[] dots = {"", ".", "..", "..."};
            int[] count = {0};

            final Timeline[] waitTL = {new Timeline()};
            boolean[] displayedConnect = {false};

            waitTL[0] = new Timeline(new KeyFrame(Duration.millis(500), a-> {
                if (clientConnection.isMatched == false) {
                    instructions.setText("Waiting for client" + dots[count[0]]);
                    count[0]++;
                }
                else if (gameStarted == false) {
                    if (displayedConnect[0] == false) {
                        System.out.println("CONNECTED!");
                        displayedConnect[0] = true;

                        clientConnection.send(guiGrid); // ONLY SEND THESE ONCE OR WE HAVE ISSUES IN THIS TIMELINE!
                    }

                    instructions.setText("Waiting for client" + dots[count[0]]);
                    count[0]++;
                }
                else {
                    System.out.println("GAME STARTED!");
                    waitTL[0].stop();

                    stackPane.getChildren().remove(vB);
                    stackPane.getChildren().remove(hB);
                    stackPane.getChildren().remove(rB);
                    stackPane.getChildren().remove(cB);
                    stackPane.getChildren().remove(humanB);
                    stackPane.getChildren().remove(aiB);
                    stackPane.getChildren().remove(logoView);
                    stackPane.getChildren().remove(instructions);
                    stackPane.getChildren().remove(gameTypeLabel);
                    stackPane.getChildren().add(dLabel);
                    stackPane.setAlignment(dLabel, Pos.TOP_RIGHT);
                }

                if (count[0] == 4) {
                    count[0] = 0;
                }
            }));

            waitTL[0].setCycleCount(Animation.INDEFINITE);
            waitTL[0].play();
        });

        stackPane.setStyle("-fx-background-color: black");

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Battleship");
        primaryStage.show();

        mediaView.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (placedPiece[0] > 4) {
                    return;
                }

                int inX = (int) mouseEvent.getX();
                int inY = (int) mouseEvent.getY();

                int adjX = inX - 98;
                int adjY = inY - 98;

                int iX, iY;

                if (adjX <= 0 || adjY <= 0) {
                    // DO NOTHING
                }
                else {
                    iX = adjX / 90;
                    iY = adjY / 90;
                    System.out.println("X: " + iX + " Y: " + iY);

                    if (!validatePlacement(iX, iY, placeMode, lengths[placedPiece[0]])) {
                        return;
                    }

                    placeShip(iX, iY, placeMode, lengths[placedPiece[0]], shipSym[placedPiece[0]]);

                    String imgDir;
                    int outDir = 0; // 0 for v, 1 for h
                    if (placeMode == 'v') {
                        imgDir = fileNamesV[placedPiece[0]];
                    }
                    else {
                        imgDir = fileNamesH[placedPiece[0]];
                        outDir = 1;
                    }

                    int[] outPlacement = {iX, iY, outDir};

                    Image ship = new Image(imgDir);
                    ImageView shipView = new ImageView(ship);
                    splash.play();
                    placedPiece[0]++;

                    stackPane.getChildren().add(shipView);
                    stackPane.setAlignment(shipView, Pos.TOP_LEFT);
                    shipView.setTranslateX(((iX * 90) + 99));
                    shipView.setTranslateY(((iY * 90) + 99));

                    if (placedPiece[0] < 5) {
                        instructions.setText("PLACE YOUR " + labelNames[placedPiece[0]]);
                    }
                    else {
                        instructions.setText("PRESS CONTINUE");
                        //printBoard();
                        cB.setDisable(false);
                    }

                }
            }
        });

        mediaViewAttack.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (attackCompleted == true) {
                    return;
                }

                int inX = (int) mouseEvent.getX();
                int inY = (int) mouseEvent.getY();

                int adjX = inX - 98;
                int adjY = inY - 98;

                int iX, iY;

                if (adjX <= 0 || adjY <= 0) {
                    // DO NOTHING
                }
                else {
                    iX = adjX / 90;
                    iY = adjY / 90;
                    System.out.println("X: " + iX + " Y: " + iY);

                    if (enemyGrid[iY][iX] == 'X' || enemyGrid[iY][iX] == 'O') {
                        return;
                    }

                    attackCompleted = true;

                    String imgDir = "";

                    if (enemyGrid[iY][iX] == 'E') { // Miss
                        enemyGrid[iY][iX] = 'O';
                        splash.play();
                        imgDir = "miss.png";
                    }
                    else { // Hit
                        enemyGrid[iY][iX] = 'X';
                        explosion.play();
                        imgDir = "hit.png";
                    }

                    Image hitOrMissImg = new Image(imgDir);
                    ImageView hitOrMissView = new ImageView(hitOrMissImg);

                    attackPane.getChildren().add(hitOrMissView);
                    attackPane.setAlignment(hitOrMissView, Pos.TOP_LEFT);
                    hitOrMissView.setTranslateX(((iX * 90) + 99));
                    hitOrMissView.setTranslateY(((iY * 90) + 99));

                    int[] outCoords = {iX, iY};
                    clientConnection.send(new Message(1, outCoords));
                }
            }
        });
    }
}
