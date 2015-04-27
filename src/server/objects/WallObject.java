package server.objects;

/**
 * Wall object, represent a wall on map.
 * Player can't go throw a wall.
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
public class WallObject extends GameObject {
    public WallObject (){
        super('w');
    }
    public boolean canGo() {
        return false;
    }
    public boolean canOpen() {
        return false;
    }
    public boolean foundKey() {
        return false;
    }
    public boolean foundTarget() {
        return false;
    }
}
