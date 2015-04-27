package server.objects;

/**
 * Main object class. Used as parent class of
 * all another game objects.
 * @author Mark Birger (xbirge00), Daniil Khudiakov (xkhudi00)
 */
abstract public class GameObject {
    public char type;

    /**
     * Constructor for definition of character
     * at each type of the games objects.
     * @param type character, which defines type (wtgk)
     */
    public GameObject(char type) {
        this.type = type;
    }
    abstract public boolean canOpen();
    abstract public boolean foundKey();
    abstract public boolean canGo();
    abstract public boolean foundTarget();
}
