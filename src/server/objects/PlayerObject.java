package server.objects;

import server.Labyrinth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

/**
 * Player object. Create for each player,
 * Player also is game object.
 * This class provides common player movements
 * and operation such a keys/gates operation.
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
public class PlayerObject extends GameObject {
    /**
     * Data type for player view.
     */
    public enum Way {
        UP, RIGHT, DOWN, LEFT;

        /**
         * Turn player view to right.
         * @return new view position
         */
        public Way right(){
            return values()[(ordinal()+1)==values().length?UP.ordinal():(ordinal()+1)];
        }

        /**
         * Turn player view to left.
         * @return new view position
         */
        public Way left(){
            return values()[(ordinal()-1)<0?LEFT.ordinal():(ordinal()-1)];
        }
    }
    public Way view;
    Labyrinth currentLab;
    public int [] position;
    int keys;
    public Socket playerSocket;
    BufferedReader in;
    PrintWriter out;
    Date comingInGame;
    boolean going = false;
    public boolean alive = true;

    /**
     * New player constructor.
     * Creates new player at position.
     * @param currentLab provide labyrinth to player object
     * @param r row position
     * @param c column position
     */
    public PlayerObject(Labyrinth currentLab,int r,int c, Socket socket, char color, Date time) {
        super(color);
        this.comingInGame = time;
        this.playerSocket = socket;
        this.view=Way.DOWN;
        this.currentLab = currentLab;
        this.position = new int [2];
        this.position[0]=r;
        this.position[1]=c;
        this.keys=0;
        try{
            in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            out = new PrintWriter(playerSocket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Player can't fo throw another.
     * @return false
     */
    public boolean canGo() {
        return false;
    }

    /**
     * Player can't be opened.
     * @return false
     */
    public boolean canOpen() {
        return false;
    }

    /**
     * Player can't be a key.
     * @return false
     */
    public boolean foundKey() {
        return false;
    }

    /**
     * Player can't be a target.
     * @return false
     */
    public boolean foundTarget() {
        return false;
    }

    /**
     * Turn player view to the left.
     * Examples: DOWN -> RIGHT.
     * @return new position name
     */
    public String turnLeft() {
        this.view = this.view.left();
        return this.view.name();
    }

    /**
     * Turn player view to the right.
     * Examples: DOWN -> LEFT.
     * @return new position name
     */
    public String turnRight() {
        this.view = this.view.right();
        return this.view.name();
    }

    /**
     * Helper method for other method.
     * Used for next position selection.
     * @param position current position
     * @return next position
     */
    public int[] getFront (int[] position) {
        int[] newPosition = new int [2];
        if (this.view == Way.UP) {
            newPosition[0] = position[0] - 1;
            newPosition[1] = position[1];
        } else if (this.view == Way.RIGHT) {
            newPosition[0] = position[0];
            newPosition[1] = position[1] + 1;
        } else if (this.view == Way.DOWN) {
            newPosition[0] = position[0] + 1;
            newPosition[1] = position[1];
        } else if (this.view == Way.LEFT) {
            newPosition[0] = position[0];
            newPosition[1] = position[1] - 1;
        }
        return newPosition;
    }

    /**
     * Player do step in current direction.
     * @return null or string with new position
     */
    public String step() {
        //System.out.print("Trying go to ");
        //System.out.println(this.view);
        int [] newPosition =  getFront(this.position);
        GameObject inFront = currentLab.getField(newPosition[0],newPosition[1]);
        while (inFront instanceof GateObject) {
            if (inFront.canGo()) {
                newPosition = getFront(newPosition);
                inFront = currentLab.getField(newPosition[0],newPosition[1]);
            } else {
                return null;
            }
        }
        if ((inFront != null) && inFront.foundTarget()) {
            return "Target founded";
        }
        if (inFront instanceof NPCObject) {
            return "You die";
        }
        if ((inFront == null) || (inFront.canGo())) {
            currentLab.setField(this, newPosition[0], newPosition[1]);
            currentLab.clearField(this.position[0], this.position[1]);
            this.position[0] = newPosition[0];
            this.position[1] = newPosition[1];
            return Arrays.toString(this.position);
        }
        return null;
    }

    /**
     * Setup player movement flag
     */
    public String letsGo() {
        this.going = true;
        return this.implMove();
    }

    /**
     * Player moves while going is enabled
     */
    public String implMove() {
        if (this.going) {
            String result = step();
            if (result == null) {
                this.going = false;
            } else {
                return result;
            }
        }
        return "No moves";
    }

    /**
     * Take key in front of player.
     * @return true/false if key founded
     */
    public boolean take() {
        int[] newPosition = getFront(this.position);
        GameObject inFront = currentLab.getField(newPosition[0], newPosition[1]);
        while (inFront instanceof GateObject) {
            if (inFront.canGo()) {
                newPosition = getFront(newPosition);
                inFront = currentLab.getField(newPosition[0], newPosition[1]);
            } else {
                return false;
            }
        }
        if ((inFront != null) && (inFront.foundKey())) {
            this.keys++;
            currentLab.clearField(newPosition[0], newPosition[1]);
            return true;
        }
        return false;
    }

    /**
     * Trying to open gate in front of the player.
     * @return true/false if gate was opened
     */
    public boolean open() {
        int[] newPosition = getFront(this.position);
        GameObject inFront = currentLab.getField(newPosition[0], newPosition[1]);
        while ((inFront instanceof GateObject)&&inFront.canGo()) {
            newPosition = getFront(newPosition);
            inFront = currentLab.getField(newPosition[0], newPosition[1]);
        }
        if ((inFront != null)&& (inFront.canOpen()) && this.keys>0) {
            this.keys--;
            GateObject closedGate = (GateObject) inFront;
            closedGate.open();
            return true;
        }
        return false;
    }

    /**
     * Current keys count.
     * @return number of keys.
     */
    public int keys() {
        return this.keys;
    }


    public String receiveMessage() {
        try {
            if (in.ready())
                return in.readLine();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }


    public void sendMessage(String output) {
        out.println(output);
        System.out.println(output);
    }
 }