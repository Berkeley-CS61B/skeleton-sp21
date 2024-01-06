package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *
 *  @author xUser5000
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.exitWithMessage("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
    }
}
