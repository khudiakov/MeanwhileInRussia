package server.objects;

/**
 * Gate object class, extends GameObject
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
public class GateObject extends GameObject {
    /**
     * Constructor for definition of special character.
     */
    public GateObject (){
        super('g');
    }
    public boolean opened=false;

    /**
     * Checks gate. Is gate opened?
     * @return true/false
     */
    public boolean canGo() {
        return opened;
    }

    /**
     * Checks gate. Is gate closed?
     * @return true/false
     */
    public boolean canOpen() {
        return !opened;
    }

    /**
     * Gate is not key.
     * @return false
     */
    public boolean foundKey() {
        return false;
    }

    /**
     * Gate is not target.
     * @return false
     */
    public boolean foundTarget() {
        return false;
    }

    /**
     * This method opens this gate.
     */
    public void open() {
        this.opened = true;
    }
}
