package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author xUser5000
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File STAGED_DIR = join(GITLET_DIR, "staged");
    public static final File ADDITION_DIR = join(STAGED_DIR, "addition");
    public static final File REMOVAL_DIR = join(STAGED_DIR, "removal");

    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            exitWithMessage("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        STAGED_DIR.mkdir();
        ADDITION_DIR.mkdir();
        REMOVAL_DIR.mkdir();
        HEAD_FILE.createNewFile();
    }
}
