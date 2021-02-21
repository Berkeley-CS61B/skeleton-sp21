package gitlet;

import java.io.Serializable;

/** An interface describing dumpable objects.
 *  @author P. N. Hilfinger
 */
interface Dumpable extends Serializable {
    /** Print useful information about this object on System.out. */
    void dump();
}
