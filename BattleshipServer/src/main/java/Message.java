import java.io.Serializable;
import java.util.ArrayList;
public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    int protocol; // 0 for turn info, 1 for attackCoords
    boolean yourTurn;
    int[] attackCoords;
    Character[][] outGrid = new Character[10][10];
    boolean isHit;

    Message (int p) {
        protocol = p;
    }

    Message (int p, boolean t) {
        protocol = p;
        yourTurn = t;
    }

    Message (int p, int[] coords) {
        protocol = p;
        attackCoords = coords;
    }

    Message (int p, int[] coords, boolean result) {
        protocol = p;
        attackCoords = coords;
        isHit = result;
    }
}


