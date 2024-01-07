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

        List<File> addedFiles = Objects.requireNonNull(plainFilenamesIn(ADDITION_DIR))
                .stream()
                .map(fileName -> join(ADDITION_DIR, fileName))
                .collect(Collectors.toList());
        List<File> removedFiles = Objects.requireNonNull(plainFilenamesIn(REMOVAL_DIR))
                .stream()
                .map(fileName -> join(REMOVAL_DIR, fileName))
                .collect(Collectors.toList());
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

    /**
     * If the file is neither staged nor tracked by the head commit, print the error message `No reason to remove the file.`
     * If the file is currently staged for addition, unstage it
     * If the file is tracked in the current commit,
     *      - stage it for removal
     *      - remove it from the working directory if the user has not already done so
     */
    public static void rm(String filename) {
        File stagedForAdditionFile = join(ADDITION_DIR, filename);
        Map<String, String> trackedFiles = getCurrentCommit().getTrackedFiles();
        if (!stagedForAdditionFile.exists() && !trackedFiles.containsKey(filename)) {
            exitWithMessage("No reason to remove the file.");
        }
        if (stagedForAdditionFile.exists()) {
            stagedForAdditionFile.delete();
        }
        if (trackedFiles.containsKey(filename)) {
            File trackedFile = join(BLOBS_DIR, trackedFiles.get(filename));
            File stagedForRemovalFile = join(REMOVAL_DIR, filename);
            writeContents(stagedForRemovalFile, readContentsAsString(trackedFile));
            File workingFile = new File(filename);
            if (workingFile.exists()) workingFile.delete();
        }
    }

    /**
     * Display information about each commit backwards along the commit tree
     *      starting from the commit in the current HEAD until the initial commit
     * Following the first parent commit links, ignoring any secondary parents found in merge commits
     * For every node in this history, the information it should display is:
     *      the commit id, the time the commit was made, and the commit message
     * Example:
     * ```
     * ===
     * commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
     * Date: Thu Nov 9 20:00:05 2017 -0800
     * A commit message.
     *
     * ===
     * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     * Date: Thu Nov 9 17:01:33 2017 -0800
     * Another commit message.
     *
     * ===
     * commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
     * Date: Wed Dec 31 16:00:00 1969 -0800
     * initial commit
     * ```
     * There is a === before each commit and an empty line after it
     * Each entry displays the unique SHA-1 id of the commit object
     * The timestamps displayed in the commits reflect the current timezone, not UTC
     * For merge commits (those that have two parent commits), add a line just below the first, as in
     * ```
     * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     * Merge: 4975af1 2c1ead1
     * Date: Sat Nov 11 12:30:00 2017 -0800
     * Merged development into master.
     * ```
     * The two hexadecimal numerals following “Merge:” consist of the first seven digits of the first and second parents’ commit ids
     * The first parent is the branch you were on when you did the merge; the second is that of the merged-in branch
     */
    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (true) {
            System.out.print(currentCommit.log());
            if (currentCommit.getParent() == null) break;
            currentCommit = Commit.fromFile(COMMITS_DIR, currentCommit.getParent());
        }
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter
     */
    public static void globalLog() {
       List<String> commitHashes = plainFilenamesIn(COMMITS_DIR);
       commitHashes.forEach(hash -> {
           Commit commit = Commit.fromFile(COMMITS_DIR, hash);
           System.out.print(commit.log());
       });
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line
     * Failure cases: If no such commit exists, prints the error message `Found no commit with that message.`
     */
    public static void find(String commitMessage) {
        List<String> matchedCommitHashes = Objects.requireNonNull(plainFilenamesIn(COMMITS_DIR))
                .stream()
                .map(hash -> Commit.fromFile(COMMITS_DIR, hash))
                .filter(commit -> commit.getMessage().equals(commitMessage))
                .map(Commit::getHash)
                .collect(Collectors.toList());
        if (matchedCommitHashes.isEmpty()) {
            exitWithMessage("Found no commit with that message.");
        }
        matchedCommitHashes.forEach(System.out::println);
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *
     * Displays what files have been staged for addition or removal
     * Example:
     * ```
     * === Branches ===
     * *master
     * other-branch
     *
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     *
     * === Removed Files ===
     * goodbye.txt
     *
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     *
     * === Untracked Files ===
     * random.stuff
     * ```
     * There is an empty line between sections, and the entire status ends in an empty line as well
     * Entries should be listed in lexicographic order,
     *      using the Java string-comparison order (the asterisk doesn’t count)
     * A file in the working directory is “modified but not staged” if it is
     *      - Tracked in the current commit, changed in the working directory, but not staged; or
     *      - Staged for addition, but with different contents than in the working directory; or
     *      - Staged for addition, but deleted in the working directory; or
     *      - Not staged for removal, but tracked in the current commit and deleted from the working directory.
     * “Untracked Files” is for files present in the working directory but neither staged for addition nor tracked
     *      - This includes files that have been staged for removal, but then re-created without Gitlet’s knowledge
     */
    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        Branch currentBranch = getCurrentBranch();
        branches.forEach(branch -> {
            if (branch.equals(currentBranch.getName())) {
                System.out.print("*");
                System.out.println(branch);
            }
        });
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> addedFiles = plainFilenamesIn(ADDITION_DIR);
        addedFiles.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> removedFiles = plainFilenamesIn(REMOVAL_DIR);
        removedFiles.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        // TODO: Modifications not staged for commit
        System.out.println();

        System.out.println("=== Untracked Files ===");
        // TODO: Untracked files
        System.out.println();
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
