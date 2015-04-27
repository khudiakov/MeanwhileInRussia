package server;

import server.objects.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.lang.NullPointerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Main game class, provide server implementation
 * this class controls all server activity,
 * including all games services.</p>
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
public class Labyrinth implements Runnable{
    final char[] playerColor = {'R', 'G', 'B', 'W'};
    public int labID;
    GameObject[][] matrix;
    int rows, columns;
    static List<PlayerObject> Players;
    static List<NPCObject> NPCs;
    PlayerObject newPlayer = null, exitPlayer = null;
    ServerSocket gameSocket;
    Date serverStart;
    float delay;
    /**
     * <p>Level loading method.
     * In future implementation can be used for every connection.</p>
     * @param filename name of the game level
     * @return true if file loaded, false if not
     */
    public boolean load(String filename) {
        BufferedReader reader = null;
        try {
                InputStream is = getClass().getResourceAsStream("examples\\" + filename);
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String levelSize = reader.readLine();
                String[] sizes = levelSize.split("x");
                this.rows = Integer.parseInt(sizes[0]);
                this.columns = Integer.parseInt(sizes[1]);
                this.matrix = new GameObject [this.rows][this.columns];
                int c, counter = 0;
                while (((c = reader.read()) != -1)&&(counter<this.rows*this.columns)) {
                    if (c == 'w') {
                        this.matrix[counter/columns][counter%columns]= new WallObject();
                    } else if (c == 'k') {
                        this.matrix[counter/columns][counter%columns]= new KeyObject();
                    } else if (c == 't') {
                        this.matrix[counter/columns][counter%columns]= new TargetObject();
                    } else if (c == 'g') {
                        this.matrix[counter/columns][counter%columns]= new GateObject();
                    } else if (c == 'e') {
                        this.matrix[counter/columns][counter%columns]= null;
                    } else if (c == '\n' || c=='\r'){
                        counter--;
                    } else if (c == 'L') {
                        NPCs.add(new NPCObject(this, counter/columns, counter%columns));
                        this.matrix[counter/columns][counter%columns]= NPCs.get(NPCs.size()-1);
                    } else {
                        return false; //wrong format
                    }
                    counter++;
                }
                if (counter != this.rows*this.columns) {
                    return false; //wrong format
                }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                reader.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Player creation method.
     * creates new player at position.
     * Returned player object provides game methods.
     * @return player, with acceptable operations
     */
    public PlayerObject newPlayer() {
        System.out.println("Try to create player");
        PlayerObject player;
        Socket playerSocket = acceptNewPlayer();
        System.out.println("Player accepted");
        int newPlayerN = Players.size();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (matrix[r][c] == null) {
                    System.out.println(r + " " + c);
                    player = new PlayerObject(this, r, c, playerSocket, playerColor[newPlayerN], new Date());
                    matrix[r][c] = player;
                    newPlayer = player;
                    System.out.println("Create new player with " + playerColor[newPlayerN] + " color");
                    return player;
                }
            }
        }
        return null;
    }


    public void killPlayer(PlayerObject player) {
        this.clearField(player.position[0], player.position[1]);
        player.alive = false;
    }


//    public NPCObject newNPC() {
//        NPCObject NPC;
//        for (int r = 0; r < rows; r++) {
//            for (int c = 0; c < columns; c++) {
//                if (matrix[r][c] == null) {
//                    NPC = new NPCObject(this, r, c);
//                    matrix[r][c] = NPC;
//                    System.out.println("Find position for NPC");
//                    return NPC;
//                }
//            }
//        }
//        System.out.println("Cant find position for NPC");
//        return null;
//    }


    public Socket acceptNewPlayer() {
        Socket playerSocket = null;
        try {
            playerSocket = gameSocket.accept();
            return playerSocket;
        }catch (Exception e) {
            System.out.println("Exception in acceptNewPlayer" + e);
        }
        return playerSocket;
    }


    //TODO circular matrix
    /**
     * Clear field at position. Uses in movement.
     * Also used for keys "get" operation.
     * @param r row position
     * @param c column position
     */
    public void clearField (int r, int c) {
        matrix[r][c]=null;
    }

    /**
     * Set field method. Used in movement.
     * Object copy itself to next position. Than free previous.
     * @param object game object (wall,key...) which need to set
     * @param r row position
     * @param c column position
     */
    public void setField (GameObject object, int r, int c) {
        matrix[r][c]=object;
    }

    /**
     * Return object for position.
     * Needed for keys/gate operations.
     * @param r row position
     * @param c column position
     * @return game object at this position
     */
    public GameObject getField (int r, int c) {
        return matrix[r][c];
    }


    public int releaseCommand(PlayerObject player, String cmd) {
        String result;
        if (cmd.equals("step")) {
            String position = player.step();
            if (position == null) {
                result = "Can't move this way.";
                System.out.println(result);
                player.sendMessage(result);
            } else if (position.equals("Target founded")) {
                result = "You are win!";
                System.out.println(result);
                player.sendMessage(result);
                return 1;
            } else if (position.equals("You die")) {
                result = "You are dead!";
                System.out.println(result);
                player.sendMessage(result);
                killPlayer(player);
            }
            else {
                result = "Okay, current position: "+position;
                System.out.println(result);
                player.sendMessage(result);
            }
        } else if (cmd.equals("go")) {
            String position = player.letsGo();
            if (position == null) {
                result = "Can't move this way.";
                System.out.println(result);
                player.sendMessage(result);
            } else if (position.equals("Target founded")) {
                result = "You are win!";
                System.out.println(result);
                player.sendMessage(result);
                //System.out.println("Goodbye!");
                return 1;
            } else if (position.equals("You die")) {
                result = "You are dead!";
                System.out.println(result);
                player.sendMessage(result);
                killPlayer(player);
                return 1;
            } else {
                result = "Okay, current position: "+position;
                System.out.println(result);
                player.sendMessage(result);
            }
        } else if (cmd.equals("left")) {
            String way = player.turnLeft();
            result = "Okay, now i'm looking "+way;
            System.out.println(result);
            player.sendMessage(result);
        } else if (cmd.equals("right")) {
            String way = player.turnRight();
            result = "Okay, now i'm looking "+way;
            System.out.println(result);
            player.sendMessage(result);
        } else if (cmd.equals("take")) {
            if (player.take()) {
                result = "Key grabbed!";
                System.out.println(result);
                player.sendMessage(result);
            } else {
                result = "There is no key.";
                System.out.println(result);
                player.sendMessage(result);
            }
        } else if (cmd.equals("open")) {
            if (player.open()) {
                result = "Door opened!";
                System.out.println(result);
                player.sendMessage(result);
            } else {
                result = "I can't open it.";
                System.out.println(result);
                player.sendMessage(result);
            }
        } else if (cmd.equals("keys")) {
            result = "You have "+player.keys()+" keys.";
            System.out.println(result);
            player.sendMessage(result);
        } else {
            result = "Unrecognized command :(";
            System.out.println(result);
            player.sendMessage(result);
        }
        return 0;
    }

    @Override
    public void run() {
        socketParse();
    }


    public void sendMap() {
        String output = "";
        for (GameObject[] objectArray : matrix) {
            for (GameObject object : objectArray) {
                if (object == null) {
                    output += '_';
                }
                else {
                    char objectSym=object.type;
                    if (object instanceof PlayerObject){
                        objectSym += ((PlayerObject) object).view.ordinal();
                    }
                    if (object instanceof NPCObject) {
                        objectSym += ((NPCObject) object).view.ordinal();
                    }
                    if (object instanceof GateObject) {
                        objectSym += (((GateObject) object).opened?1:0);
                    }
                    output += objectSym;
                }
            }
        }
        for (PlayerObject player: Players) {
            player.sendMessage(output);
        }
    }
    public void sendMap(PlayerObject player) {
        String output = "";
        for (GameObject[] objectArray : matrix) {
            for (GameObject object : objectArray) {
                if (object == null) {
                    output += '_';
                }
                else {
                    output += object.type;
                }
            }
        }
        player.sendMessage(output);
    }


    public void socketParse () {
        String cmd=null;
        int cmdResult=0;
        while (cmdResult != 1) {
            try {
                if (newPlayer!=null) {
                    Players.add(newPlayer);
                    newPlayer=null;
                }
                for (PlayerObject actualPlayer:Players) {
                    System.out.println("Moves " + actualPlayer.type + " player");
                    cmd = actualPlayer.receiveMessage();
                    System.out.println(actualPlayer.type + " player send " + cmd + "\nCommand result:");
                    if (cmd == null) {
                        actualPlayer.implMove();
                    }
                    else if (cmd.equals("hello")) {
                        String output = actualPlayer.type + " " + rows + " " + columns;
                        actualPlayer.sendMessage(output);
                        System.out.println(output);
                    } else if (cmd.equals("exit")) {
                        killPlayer(actualPlayer);
                        exitPlayer = actualPlayer;
                        actualPlayer.playerSocket.close();
                    }
                    else {
                        cmdResult = releaseCommand(actualPlayer, cmd);
                    }
                }
                for (NPCObject myNPC:NPCs) {
                    myNPC.doAnything();
                }
                sendMap();
                Thread.sleep(Math.round(delay * 1000));
                if (exitPlayer!=null) {
                    Players.remove(exitPlayer);
                    if (Players.size() == 0) {
                        break;
                    }
                }
                for (PlayerObject player : Players) {
                    if (player.alive) {
                        player.sendMessage((cmdResult == 0 ? "next" : "gameover"));
                    }
                    else {
                        player.sendMessage("dead");
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception in socketParse " + e);
                System.out.println(Players.size());
                System.out.println(NPCs.size());
            }
        }
        try {
            for (PlayerObject actualPlayer : Players) {
                actualPlayer.playerSocket.close();
            }
            UserConnect.DeleteGame(labID);
            gameSocket.close();
        } catch (Exception e) {
            System.out.println("Exception in socketParse: "+e);
            System.out.println(Players.size());
            System.out.println(NPCs.size());
        }
        System.out.println("Game ended");
    }

    /**
     * Main method. Provides command line interface.
     * Operations:
     *  game <level>    load level
     *  close           close game
     *  show            prints map
     *  step            do one step
     *  left            turn left
     *  right           turn right
     *  take            take keys in front
     *  open            open gate in front
     *  keys            prints number of keys
     */

    public Labyrinth(ServerSocket socket, float gameDelay, String map) {
        try {
            System.out.println("Hello at the awesome server!");
            serverStart = new Date();
            System.out.println("Server start at " + serverStart.toString());
            gameSocket = socket;
            Players = new ArrayList<PlayerObject>();
            NPCs = new ArrayList<NPCObject>();
            delay=gameDelay;
            System.out.println("Hello at the awesome server!");
            load(map);
            //NPCs.add(newNPC());
        } catch(Exception e){
            System.out.println("Error "+e.getMessage());
        }
    }
}
