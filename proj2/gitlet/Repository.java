package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * If the commit message is empty, print the error message `Please enter a commit message.`
     * If no files have been staged, print the message `No changes added to the commit.`
     * For each added/modified files in the staging area,
     *      - Compute its SHA-1 hash as a function of its contents
     *      - Copy it to BLOBS_DIR and set its name to its hash value
     * Build the commit:
     *      - Set the commit message
     *      - Set the timestamp to now
     *      - Reuse all tracked files from the parent commit
     *      - Rewire the references of the added/modified files
     *      - Untrack removed files (i.e, files in REMOVAL_DIR)
     *      - Point to the parent commit
     * Save the commit on disk in COMMITS_DIR
     * Update the pointer of the current branch to point to the new commit
     * Clear the staging area (i.e, remove all files in ADDITION_DIR and REMOVAL_DIR)
     *
     */
    public static void commit(String message) {
        if (message.isEmpty()) {
            exitWithMessage("Please enter a commit message.");
        }

        List<File> addedFiles = Objects.requireNonNull(plainFilenamesIn(ADDITION_DIR)).stream().map(File::new).collect(Collectors.toList());
        List<File> removedFiles = Objects.requireNonNull(plainFilenamesIn(REMOVAL_DIR)).stream().map(File::new).collect(Collectors.toList());
        if (addedFiles.isEmpty() && removedFiles.isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }

        /* Compute the SHA-1 hash of added/modified files and store them in the blobs directory */
        Map<String, String> nameToBlob = new TreeMap<>();
        addedFiles.forEach(file -> {
            String hash = storeBlob(file);
            nameToBlob.put(file.getName(), hash);
        });

        /* update the tracked list of files */
        Commit currentCommit = getCurrentCommit();
        Map<String, String> trackedFiles = currentCommit.getTrackedFiles();
        trackedFiles.putAll(nameToBlob);
        removedFiles.forEach(file -> trackedFiles.remove(file.getName()));

        /* Build and save the commit */
        Commit newCommit = new Commit(message, new Date(), trackedFiles, currentCommit.getHash(), null);
        newCommit.saveCommit(COMMITS_DIR);

        /* Update the pointer of the current branch to point to the newly-created commit */
        Branch branch = getCurrentBranch();
        branch.setHead(newCommit.getHash());
        branch.saveBranch(BRANCHES_DIR);

        /* Clear the staging area */
        addedFiles.forEach(File::delete);
        removedFiles.forEach(File::delete);
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

    private static String storeBlob(File file) {
        String contents = readContentsAsString(file);
        String hash = sha1(contents);
        File storedBlob = join(BLOBS_DIR, hash);
        writeContents(storedBlob, contents);
        return hash;
    }
}
