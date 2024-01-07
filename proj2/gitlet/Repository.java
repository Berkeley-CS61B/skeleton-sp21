package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;

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

    /**
     * Initialize the directory structure inside .gitlet
     * Start with a single branch called "master"
     * Start with one commit with the message "initial commit" with a timestamp = Unix epoch
     * Set HEAD to point to the master branch
     */
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

        Commit initialCommit = new Commit("initial commit", new Date(0), null, null, null);
        initialCommit.saveCommit(COMMITS_DIR);

        Branch masterBranch = new Branch("master", initialCommit.getHash());
        masterBranch.saveBranch(BRANCHES_DIR);

        setCurrentBranch(masterBranch.getName());
    }

    /**
     * If the file does not exist, print the error message `File does not exist.` and exit without changing anything.
     * If the current working version of the file is identical to the version in the current commit,
     *      do not stage it to be added, and remove it from the staging area if it is already there.
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * If the file was staged for removal, it will no longer be after executing the command
     * Make a copy of the file to .gitlet/STAGED_DIR/ADDITION_DIR/file_name
     *      - we are dealing with a flat directory structure
     */
    public static void add(String fileName) {
        File workingFile = new File(fileName);
        if (!workingFile.exists()) {
            exitWithMessage("File does not exist.");
        }
        String workingFileContents = readContentsAsString(workingFile);
        File stagedFile = join(ADDITION_DIR, fileName);
        Commit currentCommit = getCurrentCommit();
        String committedFileHash = currentCommit.getTrackedFiles().get(fileName);
        if (sha1(workingFileContents).equals(committedFileHash)) {
            if (stagedFile.exists()) stagedFile.delete();
            return;
        }
        writeContents(stagedFile, workingFileContents);
        File stagedForRemovalFile = new File(REMOVAL_DIR, fileName);
        if (stagedForRemovalFile.exists()) {
            stagedForRemovalFile.delete();
        }
    }

    private static Commit getCurrentCommit() {
        String commitHash = getCurrentBranch().getHead();
        return Commit.fromFile(COMMITS_DIR, commitHash);
    }

    private static Branch getCurrentBranch() {
        String branchName = readContentsAsString(HEAD_FILE);
        return Branch.fromFile(BRANCHES_DIR, branchName);
    }

    private static void setCurrentBranch(String branchName) {
        writeContents(HEAD_FILE, branchName);
    }
}
