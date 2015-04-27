package server.objects;

/**
 * Key object, represent a key on map.
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
public class KeyObject extends GameObject {
    /**
     * Constructor defines special character.
     */
    public KeyObject (){
        super('k');
    }

    /**
     * You can't go throw the key.
     * @return false
     */
    public boolean canGo() {
        return false;
    }

    /**
     * Key can't be opened.
     * @return false
     */
    public boolean canOpen() {
        return false;
    }

    /**
     * Method check, this key is a key?
     * @return true
     */
    public boolean foundKey() {
        return true;
    }

    /**
     * Method cheks for target. Key isn't target.
     * @return false
     */
    public boolean foundTarget() {
        return false;
    }
}
