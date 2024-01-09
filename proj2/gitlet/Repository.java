package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author xUser5000
 */
public class Repository {

    /** The current working directory. */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private static final File GITLET_DIR = join(CWD, ".gitlet");

    private static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    private static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    private static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    private static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    private static final File STAGED_DIR = join(GITLET_DIR, "staged");
    private static final File ADDITION_DIR = join(STAGED_DIR, "addition");
    private static final File REMOVAL_DIR = join(STAGED_DIR, "removal");

    private static final CommitStore commitStore = new CommitStore(COMMITS_DIR);
    private static final BranchStore branchStore = new BranchStore(BRANCHES_DIR);
    private static final StagingArea stagingArea = new StagingArea(ADDITION_DIR, REMOVAL_DIR);
    private static final Head head = new Head(HEAD_FILE);
    private static final BlobStore blobStore = new BlobStore(BLOBS_DIR);

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

        Commit initialCommit = new Commit.Builder("initial commit").timestamp(new Date(0)).build();
        commitStore.saveCommit(initialCommit);

        Branch masterBranch = new Branch("master", initialCommit.getHash());
        branchStore.saveBranch(masterBranch);

        setCurrentBranch(masterBranch);
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
        File workingFile = join(CWD, fileName);
        if (!workingFile.exists()) {
            exitWithMessage("File does not exist.");
        }
        String workingFileContents = readContentsAsString(workingFile);
        File stagedFile = stagingArea.stageForAddition(workingFile);
        Commit currentCommit = getCurrentCommit();
        String committedFileHash = currentCommit.getTrackedFiles().get(fileName);
        if (sha1(workingFileContents).equals(committedFileHash)) {
            if (stagedFile != null) stagedFile.delete();
            return;
        }
        File stagedForRemovalFile = stagingArea.getFileForRemoval(fileName);
        if (stagedForRemovalFile != null) {
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

        if (stagingArea.isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }

        List<File> addedFiles = stagingArea.getFilesForAddition();
        List<File> removedFiles = stagingArea.getFilesForRemoval();

        /* Compute the SHA-1 hash of added/modified files and store them in the blobs directory */
        Map<String, String> nameToBlob = new TreeMap<>();
        addedFiles.forEach(file -> {
            File storedBlob = blobStore.save(file);
            nameToBlob.put(file.getName(), storedBlob.getName());
        });

        /* update the tracked list of files */
        Commit currentCommit = getCurrentCommit();
        Map<String, String> trackedFiles = currentCommit.getTrackedFiles();
        trackedFiles.putAll(nameToBlob);
        removedFiles.forEach(file -> trackedFiles.remove(file.getName()));

        /* Build and save the commit */
        Commit newCommit = new Commit.Builder(message)
                .parent(currentCommit.getHash())
                .trackedFiles(trackedFiles)
                .build();
        commitStore.saveCommit(newCommit);

        /* Update the pointer of the current branch to point to the newly-created commit */
        Branch branch = getCurrentBranch();
        branch.setHead(newCommit.getHash());
        branchStore.saveBranch(branch);

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
        File stagedForAdditionFile = stagingArea.getFileForAddition(filename);
        Map<String, String> trackedFiles = getCurrentCommit().getTrackedFiles();
        if (stagedForAdditionFile == null && !trackedFiles.containsKey(filename)) {
            exitWithMessage("No reason to remove the file.");
        }
        if (stagedForAdditionFile != null) {
            stagedForAdditionFile.delete();
        }
        if (trackedFiles.containsKey(filename)) {
            File trackedFile = blobStore.get(trackedFiles.get(filename));
            stagingArea.stageForRemoval(readContentsAsString(trackedFile), filename);
            File workingFile = join(CWD, filename);
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
        while (currentCommit != null) {
            System.out.print(currentCommit.log());
            currentCommit = commitStore.getCommitById(currentCommit.getParent());
        }
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter
     */
    public static void globalLog() {
        commitStore
                .allCommitsStream()
                .map(Commit::log)
                .forEach(System.out::print);
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line
     * Failure cases: If no such commit exists, prints the error message `Found no commit with that message.`
     */
    public static void find(String commitMessage) {
        List<Commit> matchedCommits = commitStore.getCommitsByMessage(commitMessage);
        if (matchedCommits.isEmpty()) {
            exitWithMessage("Found no commit with that message.");
        }
        matchedCommits
                .stream()
                .map(Commit::getHash)
                .forEach(System.out::println);
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
        List<Branch> branches = branchStore.allBranches();
        Branch currentBranch = getCurrentBranch();
        branches.forEach(branch -> {
            if (branch.getName().equals(currentBranch.getName())) {
                System.out.print("*");
            }
            System.out.println(branch.getName());
        });
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<File> addedFiles = stagingArea.getFilesForAddition();
        addedFiles.stream().map(File::getName).forEach(System.out::println);
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<File> removedFiles = stagingArea.getFilesForRemoval();
        removedFiles.stream().map(File::getName).forEach(System.out::println);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        // TODO: Modifications not staged for commit
        System.out.println();

        System.out.println("=== Untracked Files ===");
        // TODO: Untracked files
        System.out.println();
    }

    /**
     * Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     *      overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * If no commit with the given id exists, print `No commit with that id exists.`
     * If the file does not exist in the previous commit, print the error message `File does not exist in that commit.`
     *
     */
    public static void checkoutFile(String commitHash, String fileName) {
        Commit commit = commitStore.getCommitById(commitHash);
        if (commit == null) {
            exitWithMessage("No commit with that id exists.");
        }

        String blobHash = commit.getTrackedFiles().get(fileName);
        if (blobHash == null) {
            exitWithMessage("File does not exist in that commit.");
        }

        File blob = blobStore.get(blobHash);
        String blobContents = readContentsAsString(blob);
        writeContents(join(CWD, fileName), blobContents);
    }

    /**
     * Checkout a file in the current commit
     */
    public static void checkoutFile(String fileName) {
        checkoutFile(getCurrentCommit().getHash(), fileName);
    }

    /**
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     *      overwriting the versions of the files that are already there if they exist
     * If that branch is the current branch, print `No need to checkout the current branch.`
     * If no branch with that name exists, print `No such branch exists.`
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     *      print `There is an untracked file in the way; delete it, or add and commit it first.`
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted
     * The staging area is cleared, unless the checked-out branch is the current branch
     * Given branch will now be considered the current branch (HEAD)
     */
    public static void checkoutBranch(String targetBranchName) {
        Branch currentBranch = getCurrentBranch();
        if (targetBranchName.equals(currentBranch.getName())) {
            exitWithMessage("No need to checkout the current branch.");
        }

        Branch targetBranch = branchStore.getBranch(targetBranchName);
        if (targetBranch == null) {
            exitWithMessage("No such branch exists.");
        }

        Commit currentCommit = getCurrentCommit();
        List<String> workingFiles = plainFilenamesIn(CWD);
        if (workingFiles
                .stream()
                .anyMatch(file -> !currentCommit.getTrackedFiles().containsKey(file))
        ) {
            exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        for (String file: workingFiles) {
            join(CWD, file).delete();
        }

        stagingArea.getFiles().forEach(File::delete);

        Commit targetCommit = commitStore.getCommitById(targetBranch.getHead());
        for (Map.Entry<String, String> entry: targetCommit.getTrackedFiles().entrySet()) {
            File workingFile = join(CWD, entry.getKey());
            String contents = readContentsAsString(blobStore.get(entry.getValue()));
            writeContents(workingFile, contents);
        }

        setCurrentBranch(targetBranch);
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit
     * This command does NOT immediately switch to the newly created branch (just as in real Git)
     * If a branch with the given name already exists, print the error message `A branch with that name already exists`
     */
    public static void branch(String branchName) {
        if (branchStore.getBranch(branchName) != null) {
            exitWithMessage("A branch with that name already exists");
        }
        Branch branch = new Branch(branchName, getCurrentBranch().getHead());
        branchStore.saveBranch(branch);
    }

    /**
     * Deletes the branch with the given name
     * If a branch with the given name does not exist, print `A branch with that name does not exist.`
     * If applied to the branch we are currently in, print `Cannot remove the current branch.`
     */
    public static void rmBranch(String branchName) {
        if (getCurrentBranch().getName().equals(branchName)) {
            exitWithMessage("Cannot remove the current branch.");
        }
        Branch branch = branchStore.getBranch(branchName);
        if (branch == null) {
            exitWithMessage("A branch with that name does not exist.");
        }
        branchStore.removeBranch(branch);
    }

    /**
     * Checks out all the files tracked by the given commit
     *      The [commit id] may be abbreviated as for checkout
     * If no commit with the given id exists, print `No commit with that id exists.`
     * If a working file is untracked in the current branch and would be overwritten by the reset,
     *      print `There is an untracked file in the way; delete it, or add and commit it first.`
     * Remove tracked files that are not present in that commit
     * Clear the staging area
     * Move the current branch’s head to that commit node
     */
    public static void reset(String commitHash) {
        Commit targetCommit = commitStore.getCommitById(commitHash);
        if (targetCommit == null) {
            exitWithMessage("No targetCommit with that id exists.");
        }

        List<String> workingFiles = plainFilenamesIn(CWD);
        if (workingFiles
                .stream()
                .anyMatch(file -> !targetCommit.getTrackedFiles().containsKey(file))
        ) {
            exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        workingFiles
                .stream()
                .map(fileName -> join(CWD, fileName))
                .forEach(File::delete);

        stagingArea.getFiles().forEach(File::delete);

        for (Map.Entry<String, String> entry: targetCommit.getTrackedFiles().entrySet()) {
            File workingFile = join(CWD, entry.getKey());
            String contents = readContentsAsString(blobStore.get(entry.getValue()));
            writeContents(workingFile, contents);
        }

        setCurrentCommit(targetCommit.getHash());
    }

    private static Commit getCurrentCommit() {
        String commitHash = getCurrentBranch().getHead();
        return commitStore.getCommitById(commitHash);
    }

    private static void setCurrentCommit(String commitHash) {
        Branch currentBranch = getCurrentBranch();
        currentBranch.setHead(commitHash);
        branchStore.saveBranch(currentBranch);
    }

    private static Branch getCurrentBranch() {
        return branchStore.getBranch(head.get());
    }

    private static void setCurrentBranch(Branch branch) {
        head.set(branch);
    }
}
