package server.objects;

import server.Labyrinth;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by gangbang on 11.05.2014.
 */
public class NPCObject extends GameObject {
    public enum Way {
        UP, RIGHT, DOWN, LEFT;
        public Way right(){
            return values()[(ordinal()+1)==values().length?UP.ordinal():(ordinal()+1)];
        }
        public Way left(){
            return values()[(ordinal()-1)<0?LEFT.ordinal():(ordinal()-1)];
        }
    }
    int [] position;
    public Way view;
    Labyrinth currentLab;

    public NPCObject(Labyrinth labyrinth, int r,int c) {
        super('L');
        this.view=Way.DOWN;
        this.position = new int [2];
        this.position[0]=r;
        this.position[1]=c;
        currentLab = labyrinth;
    }

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
        if (inFront instanceof PlayerObject) {
            currentLab.killPlayer((PlayerObject)inFront);
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

    public void doAnything() {
        Random iWant = new Random();
        int myChoose=iWant.nextInt(4);
        switch (myChoose) {
            case 0:
                this.step();
                break;
            case 1:
                this.turnLeft();
                break;
            case 2:
                this.turnRight();
                break;
            default:
        }
    }

    @Override
    public boolean foundTarget() {
        return false;
    }
    @Override
    public boolean foundKey() {
        return false;
    }
    @Override
    public boolean canOpen() {
        return false;
    }
    @Override
    public boolean canGo() {
        return false;
    }
}
