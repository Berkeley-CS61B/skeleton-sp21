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

    private static final WorkingArea workingArea = new WorkingArea(CWD);
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
     * Add a file in the working directory to the staging area
     * If the file does not exist, print `File does not exist.`
     * If the current working version of the file is identical to the version in the current commit,
     *      do not stage it to be added, and remove it from the staging area if it is already there.
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * Stage the file for addition
     * If the file was staged for removal, it will no longer be after executing the command
     */
    public static void add(String fileName) {
        File workingFile = workingArea.getFile(fileName);
        if (workingFile == null) {
            exitWithMessage("File does not exist.");
        }

        String committedFileHash = getCurrentCommit().getTrackedFiles().get(fileName);
        String workingFileHash = sha1(readContentsAsString(workingFile));
        if (!workingFileHash.equals(committedFileHash)) {
            stagingArea.stageForAddition(workingFile);
        } else {
            stagingArea.unstageForAddition(fileName);
        }

        stagingArea.unstageForRemoval(fileName);
    }

    /**
     * If the commit message is empty, print `Please enter a commit message.`
     * If no files have been staged, print `No changes added to the commit.`
     * For each added/modified files in the staging area,
     *      - Compute its SHA-1 hash as a function of its contents
     *      - Copy it to the blobs directory and set its to its SHA-1 hash
     * Build the commit
     *      - Reuse all tracked files from the parent commit
     *      - Rewire the references of the added/modified files
     *      - Untrack removed files
     *      - Point to the parent commit
     * Save the commit on disk
     * Update the pointer of the current branch to point to the new commit
     * Clear the staging area
     */
    public static void commit(String message) {
        if (message.isEmpty()) {
            exitWithMessage("Please enter a commit message.");
        }

        if (stagingArea.isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }

        /* Compute the SHA-1 hash of added/modified files and store them in the blobs directory */
        Map<String, String> nameToBlob = new TreeMap<>();
        stagingArea.getFilesForAddition().forEach(file -> {
            File storedBlob = blobStore.save(file);
            nameToBlob.put(file.getName(), storedBlob.getName());
        });

        /* update the tracked list of files */
        Map<String, String> trackedFiles = getCurrentCommit().getTrackedFiles();
        trackedFiles.putAll(nameToBlob);
        stagingArea.getFilesForRemoval().forEach(file -> trackedFiles.remove(file.getName()));

        Commit newCommit = new Commit.Builder(message)
                .parent(getCurrentCommit().getHash())
                .trackedFiles(trackedFiles)
                .build();
        commitStore.saveCommit(newCommit);

        Branch branch = getCurrentBranch();
        branch.setCommit(newCommit.getHash());
        branchStore.saveBranch(branch);

        stagingArea.clear();
    }

    /**
     * If the file is neither staged nor tracked by the head commit, print `No reason to remove the file.`
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
            stagingArea.unstageForAddition(filename);
        }
        if (trackedFiles.containsKey(filename)) {
            File trackedFile = blobStore.get(trackedFiles.get(filename));
            stagingArea.stageForRemoval(readContentsAsString(trackedFile), filename);
            workingArea.deleteFile(filename);
        }
    }

    /**
     * Display information about each commit backwards along the commit tree
     *      starting from the commit in the current HEAD until the initial commit
     * More info in Commit::log()
     */
    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (currentCommit != null) {
            System.out.print(currentCommit.log());
            currentCommit = commitStore.getCommitByHash(currentCommit.getParent());
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
     * If no such commit exists, print `Found no commit with that message.`
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
     * Entries are listed in lexicographic order
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
     * If no commit with the given id exists, print `No commit with that id exists.`
     * If the file does not exist in the previous commit, print `File does not exist in that commit.`
     * The new version of the file is not staged.
     */
    public static void checkoutFile(String commitHash, String fileName) {
        Commit commit = commitStore.getCommitByHash(commitHash);
        if (commit == null) {
            exitWithMessage("No commit with that id exists.");
        }

        String blobHash = commit.getTrackedFiles().get(fileName);
        if (blobHash == null) {
            exitWithMessage("File does not exist in that commit.");
        }

        File blob = blobStore.get(blobHash);
        String blobContents = readContentsAsString(blob);
        workingArea.saveFile(blobContents, fileName);
    }

    /**
     * same as checkoutFile(String commitHash, String fileName) except that commitHash is the current commit hash
     */
    public static void checkoutFile(String fileName) {
        checkoutFile(getCurrentCommit().getHash(), fileName);
    }

    /**
     * Reset the current working directory to a given commit
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     *      print `There is an untracked file in the way; delete it, or add and commit it first.`
     * Clears the staging area
     */
    private static void checkoutCommit(Commit commit) {
        Map<String, String> trackedFiles = commit.getTrackedFiles();
        if (workingArea.allFiles()
                .stream()
                .map(File::getName)
                .anyMatch(fileName -> !trackedFiles.containsKey(fileName))
        ) {
            exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        workingArea.clear();
        stagingArea.clear();

        trackedFiles.keySet().forEach(fileName -> checkoutFile(commit.getHash(), fileName));

        setCurrentCommit(commit);
    }

    /**
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     *      overwriting the versions of the files that are already there if they exist
     * If that branch is the current branch, print `No need to checkout the current branch.`
     * If no branch with that name exists, print `No such branch exists.`
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

        Commit targetCommit = commitStore.getCommitByHash(targetBranch.getCommitHash());
        checkoutCommit(targetCommit);

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
        Branch branch = new Branch(branchName, getCurrentBranch().getCommitHash());
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
        Commit targetCommit = commitStore.getCommitByHash(commitHash);
        checkoutCommit(targetCommit);
    }

    private static Commit getCurrentCommit() {
        String commitHash = getCurrentBranch().getCommitHash();
        return commitStore.getCommitByHash(commitHash);
    }

    private static void setCurrentCommit(Commit commit) {
        Branch currentBranch = getCurrentBranch();
        currentBranch.setCommit(commit);
        branchStore.saveBranch(currentBranch);
    }

    private static Branch getCurrentBranch() {
        return branchStore.getBranch(head.get());
    }

    private static void setCurrentBranch(Branch branch) {
        head.set(branch);
    }
}
