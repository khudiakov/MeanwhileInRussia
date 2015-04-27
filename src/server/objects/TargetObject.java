package server.objects;

/**
 * Target object, represent a target on map.
 * Used once at level.
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
public class TargetObject extends GameObject {
    /**
     * Constructor which specifies special character.
     */
    public TargetObject (){
        super('t');
    }

    /**
     * Player can go to the target, once.
     * @return true
     */
    public boolean canGo() {
        return true;
    }

    /**
     * Target can't be opened.
     * @return false
     */
    public boolean canOpen() {
        return false;
    }

    /**
     * Target isn't key.
     * @return false
     */
    public boolean foundKey() {
        return false;
    }

    /**
     * Target is target.
     * @return true
     */
    public boolean foundTarget() {
        return true;
    }
}
