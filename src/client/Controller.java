package client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.util.ResourceBundle;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class Controller implements Initializable {

    //connection screen
    @FXML
    private TextField host;

    @FXML
    private TextField port;

    @FXML
    private Button connect;

    @FXML
    private Label connectionStatus;

    @FXML
    private static AnchorPane connection;

    //select screen
    @FXML
    private ChoiceBox map;

    @FXML
    private ListView games;

    @FXML
    private Button create;

    @FXML
    private Button join;

    @FXML
    private TextField delay;

    @FXML
    private Label newStatus;

    @FXML
    private AnchorPane selection;

    //common
    @FXML
    private static AnchorPane menu;

    //canvas
    @FXML
    private static AnchorPane gameScreen;

    @FXML
    private static Label color;

    @FXML
    private static Label response;

    @FXML
    private static Button send;

    @FXML
    private static Button exit;

    @FXML
    private static TextField command;

    static Client communication = new Client();
    static int p;

    static int gridSize;

    static int rows;
    static int columns;

    Timer taskTimer;
    TimerTask timerTask;
    static boolean finishFlag = false;

    int timerDelay = 0;

    //controller logic
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        timerTask = new TimeTask(gameScreen);

        connect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                //change status
                connectionStatus.setVisible(true);
                //parse params
                p = 0;
                try {
                    p = Integer.parseInt(port.getText());
                } catch (NumberFormatException err) {
                    //port troubles
                    connectionStatus.setText("wrong port format");
                    return;
                }
                if ((p<1)||(p>65535)) {
                    connectionStatus.setText("wrong port value");
                    return;
                }
                //create connection
                if (!communication.Client(host.getText(), p)) {
                    connectionStatus.setText("connection failed");
                    return;
                }

                //parse maps
                String[] mapList = communication.read().split(" ");

                //parse games
                String[] gamesList = communication.read().split(" ");

                //specify selects
                map.setItems(FXCollections.observableArrayList(mapList));
                map.getSelectionModel().selectFirst();
                games.setItems(FXCollections.observableArrayList(gamesList));
                //games.getSelectionModel().selectFirst();
                //hide old panel
                connectionStatus.setText("");
                connection.setVisible(false);


            }
        });

//        join.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//
//                String selectedGame = games.getSelectionModel().getSelectedItem().toString();
//                String[] gameInfo = selectedGame.split(":");
//
//                System.out.println(gameInfo[0]);
//                System.out.println(gameInfo[1]);
//                System.out.println(gameInfo[2]);
//
//                // join id to old socket
//                communication.send("join "+gameInfo[0]);
//
//                //connect to port
//                int gp;
//                try {
//                    gp = Integer.parseInt(gameInfo[2]);
//                } catch (NumberFormatException err) {
//                    //port troubles
//                    System.out.print("received wrong port");
//                    return;
//                }
//                communication.close();
//                if (!communication.Client(host.getText(), gp)) {
//                    System.out.print("game connection failed");
//                    return;
//                }
//
//                menu.setVisible(false);
//
//                //hello
//                communication.send("hello");
//                System.out.println("read basic info");
//                String[] info = communication.read().split(" ");
//                Controller.setColor(info[0]);
//                Controller.setSize(info[1], info[2]);
//                Controller.draw();
//
//            }
//        });

        join.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                String selectedGame = games.getSelectionModel().getSelectedItem().toString();
                String[] gameInfo = selectedGame.split(":");

                System.out.println(gameInfo[0]); //id
                System.out.println(gameInfo[1]); //map
                System.out.println(gameInfo[2]); //delay
                System.out.println(gameInfo[3]); //port

                try {
                    timerDelay = Math.round(Float.parseFloat(gameInfo[2])*1000);
                } catch (NumberFormatException err) {
                    System.out.print("parse delay failure");
                    return;
                }

                // join id to old socket
                communication.send("join "+gameInfo[0]);

                //connect to port
                int gp;
                try {
                    gp = Integer.parseInt(gameInfo[3]);
                } catch (NumberFormatException err) {
                    //port troubles
                    System.out.print("received wrong port");
                    return;
                }
                communication.close();
                if (!communication.Client(host.getText(), gp)) {
                    System.out.print("game connection failed");
                    return;
                }

                menu.setVisible(false);

                //hello
                communication.send("hello");
                System.out.println("read basic info");
                String[] info = communication.read().split(" ");
                System.out.println("read basic info");
                Controller.setColor(info[0]);
                Controller.setSize(info[1], info[2]);
//                Controller.draw();
                taskTimer = new Timer();
                taskTimer.scheduleAtFixedRate(timerTask, 0, timerDelay);

            }
        });

        create.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                //parse delay
                float d = 0;
                try {
                    d = Float.parseFloat(delay.getText());
                } catch (NumberFormatException err) {
                    //port troubles
                    newStatus.setVisible(true);
                    newStatus.setText("wrong delay format");
                    return;
                }
                if ((d<0.5)||(d>5.0)) {
                    newStatus.setVisible(true);
                    newStatus.setText("wrong delay value (0.5..5)");
                    return;
                }
                timerDelay = Math.round(d * 1000);
                //send new game request
                String message = "new "+map.getValue().toString()+" "+d;
                System.out.println(message);
                System.out.println("sending port");
                communication.send(message);

                //read port from game server
                String gamePort = communication.read();
                System.out.println(gamePort);
                int gp;
                try {
                    gp = Integer.parseInt(gamePort);
                } catch (NumberFormatException err) {
                    //port troubles
                    newStatus.setText("received wrong port");
                    return;
                }

                //connect to new game port
                System.out.println("closing old port");
                communication.close();
                if (!communication.Client(host.getText(), gp)) {
                    newStatus.setText("game connection failed");
                    return;
                }

                menu.setVisible(false);

                //after join read basic info
                communication.send("hello");
                System.out.println("read basic info");
                String infos = communication.read();
                System.out.println(infos);
                String[] info = infos.split(" ");
                Controller.setColor(info[0]);
                Controller.setSize(info[1], info[2]);
//                Controller.draw();
                System.out.println("running task");
                taskTimer = new Timer();
                taskTimer.scheduleAtFixedRate(timerTask, 0, 100);
                System.out.println("closing task");

            }
        });

        send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!send.isDisabled()) {
                    communication.send(command.getText());
                    response.setText(communication.read());
                    command.setDisable(true);
                    send.setDisable(true);
//                    Controller.draw();
                }
            }
        });

        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                communication.send("exit");
                communication.close();
                send.setDisable(false);
                connection.setVisible(true);
                menu.setVisible(true);
                finishFlag = true;
            }
        });

    }

    public static void setSize(String x, String y) {
        try {
            rows = Integer.parseInt(x);
            columns = Integer.parseInt(y);
        } catch (NumberFormatException err) {
            //port troubles
            System.out.print("map size parse error");
        }
        gridSize = Math.min(800 / columns, 555 / rows);
//        gridSize = Math.round(Math.max(gridSize, 60));
    }

    public static void setColor(String s) {
        char c = s.charAt(0);
        String msg = "You are ";
        switch (c){
            case 'R':
                msg += "red";
                break;
            case 'G':
                msg += "green";
                break;
            case 'B':
                msg += "blue";
                break;
            case 'Y':
                msg += "yellow";
                break;
            default:
                System.out.println("color failure");
        }
        msg += "!";
        color.setText(msg);
    }

    class TimeTask extends TimerTask{

        final AnchorPane screen;

        public TimeTask(AnchorPane gameScreen) {
            screen = gameScreen;
        }

        @Override
        public void run() {

                if (finishFlag) {
                    timerTask.cancel();
                } else {
                    Controller.draw(screen);
                }

        }

    }

    static GridPane grid = new GridPane();
//    gridNext;

    public static void draw(AnchorPane gs) {

        final AnchorPane gsCopy = gs;

        System.out.println("Starting drawing");

        gs.setStyle("-fx-background-image: url('background.png')");

//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                gsCopy.getChildren().remove(grid);
//            }
//        });

        final GridPane gridNext = new GridPane();

        Image img = null;
        ImageView imgView;
        String state = communication.read();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                char type = '_';
                try {
                    type = state.charAt(r*columns+c);
                } catch (java.lang.NullPointerException err) {
                    //port troubles
                    System.out.print("server closed");
                    communication.close();
                    send.setDisable(false);
                    connection.setVisible(true);
                    menu.setVisible(true);
                    finishFlag = true;
                }
                switch (type) {
                    case 'w':
                        img = new Image("wall.png");
                        break;
                    case 'k':
                        img = new Image("key.png");
                        break;
                    case 'g':

                        break;
                    case 't':
                        img = new Image("target.png");
                        break;
                    case 'R'://RSTU
                        img = new Image("r_top.png");
                        break;
                    case 'S':
                        img = new Image("r_right.png");
                        break;
                    case 'T':
                        img = new Image("r_down.png");
                        break;
                    case 'U':
                        img = new Image("r_left.png");
                        break;
                    case 'G'://GHIJ
                        img = new Image("g_top.png");
                        break;
                    case 'H':
                        img = new Image("g_right.png");
                        break;
                    case 'I':
                        img = new Image("g_down.png");
                        break;
                    case 'J':
                        img = new Image("g_left.png");
                        break;
                    case 'B'://BCDE
                        img = new Image("b_top.png");
                        break;
                    case 'C':
                        img = new Image("b_right.png");
                        break;
                    case 'D':
                        img = new Image("b_down.png");
                        break;
                    case 'E':
                        img = new Image("b_left.png");
                        break;
                    case 'W'://WXYZ
                        img = new Image("y_top.png");
                        break;
                    case 'X':
                        img = new Image("y_right.png");
                        break;
                    case 'Y':
                        img = new Image("y_down.png");
                        break;
                    case 'Z':
                        img = new Image("y_left.png");
                        break;
                    case 'L'://LMNO
                        img = new Image("l_top.png");
                        break;
                    case 'M':
                        img = new Image("l_right.png");
                        break;
                    case 'N':
                        img = new Image("l_down.png");
                        break;
                    case 'O':
                        img = new Image("l_left.png");
                        break;
                    case '_':
                        continue;
                }
                imgView = new ImageView(img);
                imgView.setFitWidth(gridSize);
                imgView.setFitHeight(gridSize);
                GridPane.setRowIndex(imgView,r);
                GridPane.setColumnIndex(imgView, c);
                gridNext.getChildren().add(imgView);
            }
        }

        System.out.println(state);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                gsCopy.getChildren().remove(grid);
                gsCopy.getChildren().add(gridNext);
                grid = gridNext;
            }
        });

        String msg = communication.read();
        try {
            if (msg.equals("next")) {
                command.setDisable(false);
                send.setDisable(false);
            } else if (msg.equals("dead")) {
                command.setDisable(true);
                send.setDisable(true);
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        response.setText("You are dead!");
//                    }
//                });
            } else if (msg.equals("gameover")) {
                communication.close();
                send.setDisable(false);
                connection.setVisible(true);
                menu.setVisible(true);
                finishFlag = true;
            }
        } catch (java.lang.NullPointerException err) {
            //port troubles
            System.out.print("server closed");
            communication.close();
            send.setDisable(false);
            connection.setVisible(true);
            menu.setVisible(true);
            finishFlag = true;
        }
    }
}
