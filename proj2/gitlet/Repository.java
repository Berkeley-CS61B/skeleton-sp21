package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
        commit(message, null);
    }

    private static void commit(String message, String secondaryParent) {
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
                .secondaryParent(secondaryParent)
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
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
        getCommitChain(getCurrentCommit()).stream()
                .map(Commit::log)
                .forEach(System.out::print);
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter
     */
    public static void globalLog() {
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
        List<Commit> matchedCommits = commitStore.getCommitsByMessage(commitMessage);
        if (matchedCommits.isEmpty()) {
            exitWithMessage("Found no commit with that message.");
        }
        matchedCommits.stream()
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
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
        checkoutFile(getCurrentCommit().getHash(), fileName);
    }

    /**
     * Reset the current working directory to a given commit
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     *      print `There is an untracked file in the way; delete it, or add and commit it first.`
     * Clears the staging area
     */
    private static void checkoutCommit(Commit targetCommit) {
        if (workingArea.allFiles().stream()
                .map(File::getName)
                .filter(fileName -> !getCurrentCommit().getTrackedFiles().containsKey(fileName))
                .anyMatch(fileName -> targetCommit.getTrackedFiles().containsKey(fileName))
        ) {
            exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        workingArea.clear();
        stagingArea.clear();

        targetCommit.getTrackedFiles().keySet().forEach(fileName -> checkoutFile(targetCommit.getHash(), fileName));
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
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
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
        checkInitializedGitletDirectory();
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
     * Move the current branch’s head to that commit
     */
    public static void reset(String commitHash) {
        checkInitializedGitletDirectory();
        Commit targetCommit = commitStore.getCommitByHash(commitHash);
        if (targetCommit == null) {
            exitWithMessage("No commit with that id exists.");
        }
        checkoutCommit(targetCommit);

        Branch currentBranch = getCurrentBranch();
        currentBranch.setCommit(targetCommit);
        branchStore.saveBranch(currentBranch);
    }

    /**
     * Merges files from the given branch into the current branch.
     * If the staging area is not empty, print `You have uncommitted changes.`
     * If a branch with the given name does not exist, print `A branch with that name does not exist.`
     * If curren branch == given branch, print `Cannot merge a branch with itself.`
     * If an untracked file in the current commit would be overwritten or deleted by the merge,
     *      print `There is an untracked file in the way; delete it, or add and commit it first.`
     * If the split point is the same commit as the given branch,
     *      - do nothing
     *      - print `Given branch is an ancestor of the current branch.`
     *      - merge is complete
     * If the split point is the current branch,
     *      - check out the given branch,
     *      - print `Current branch fast-forwarded.`
     *      - merge is complete
     * Merging rules (denoting the current branch as HEAD, the given branch as OTHER, and the split commit as SPLIT):
     *      1. If the file is modified in OTHER but not HEAD, use the version in OTHER
     *      2. If the file is modified in HEAD but not OTHER, use the version in HEAD
     *      3. If the file is modified in OTHER and HEAD,
     *          3.a. if HEAD and OTHER are modified in the same way, do nothing
     *          3.b. if HEAD and OTHER are modified in different ways, conflict
     *      4. If the file is not in SPLIT nor OTHER but in HEAD, use the version in HEAD
     *      5. If the file is not in SPLIT nor HEAD but in OTHER, use the version in OTHER
     *      6. If the file is unmodified in HEAD but not present in OTHER, remove
     *      7. If the file is unmodified in OTHER but not present in HEAD, remain removed
     * Whenever the resulting file of the merge is different from its version in HEAD,
     *      add it to the staging area
     * When a conflict occurs, fill the conflicted file with the following:
     * ```
     * <<<<<<< HEAD
     * contents of file in current branch
     * =======
     * contents of file in given branch
     * >>>>>>>
     * ```
     * Once files have been updated according to the above rules,
     *      - commit with the message: `Merged [given branch name] into [current branch name].`
     *      - if the merge encountered a conflict, print the message `Encountered a merge conflict.`
     */
    public static void merge(String branchName) {
        checkInitializedGitletDirectory();
        if (!stagingArea.isEmpty()) {
            exitWithMessage("You have uncommitted changes.");
        }

        Branch targetBranch = branchStore.getBranch(branchName);
        if (targetBranch == null) {
            exitWithMessage("A branch with that name does not exist.");
        }

        Branch currentBranch = getCurrentBranch();
        if (targetBranch.getName().equals(currentBranch.getName())) {
            exitWithMessage("Cannot merge a branch with itself.");
        }

        if (workingArea.allFiles().stream()
                .anyMatch(file -> !getCurrentCommit().getTrackedFiles().containsKey(file.getName()))
        ) {
            exitWithMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        final Commit HEAD_COMMIT = commitStore.getCommitByHash(currentBranch.getCommitHash());
        final Commit OTHER_COMMIT = commitStore.getCommitByHash(targetBranch.getCommitHash());
        final Commit SPLIT_COMMIT = splitPoint(currentBranch, targetBranch);

        if (SPLIT_COMMIT.equals(OTHER_COMMIT)) {
            exitWithMessage("Given branch is an ancestor of the current branch.");
        }

        if (SPLIT_COMMIT.equals(HEAD_COMMIT)) {
            checkoutBranch(targetBranch.getName());
            exitWithMessage("Current branch fast-forwarded.");
        }

        Set<String> filePool = new HashSet<>();
        filePool.addAll(SPLIT_COMMIT.getTrackedFiles().keySet());
        filePool.addAll(HEAD_COMMIT.getTrackedFiles().keySet());
        filePool.addAll(OTHER_COMMIT.getTrackedFiles().keySet());

        AtomicBoolean isConflict = new AtomicBoolean(false);
        filePool.forEach(fileName -> {
            final String SPLIT = SPLIT_COMMIT.getTrackedFiles().get(fileName);
            final String HEAD = HEAD_COMMIT.getTrackedFiles().get(fileName);
            final String OTHER = OTHER_COMMIT.getTrackedFiles().get(fileName);

            if (SPLIT != null && OTHER != null && !SPLIT.equals(OTHER) && SPLIT.equals(HEAD)) {
                String contents = readContentsAsString(blobStore.get(OTHER));
                workingArea.saveFile(contents, fileName);
                stagingArea.stageForAddition(readContentsAsString(blobStore.get(OTHER)), fileName);
            }

            if (SPLIT != null && HEAD != null && !SPLIT.equals(HEAD) && SPLIT.equals(OTHER)) {
                workingArea.saveFile(readContentsAsString(blobStore.get(HEAD)), fileName);
            }

            if (
                    !Objects.equals(SPLIT, HEAD) && !Objects.equals(SPLIT, OTHER) && !Objects.equals(HEAD, OTHER)
            ) {
                /* conflict */
                String headContents = (HEAD != null ? readContentsAsString(blobStore.get(HEAD)) : "");
                String otherContents = (OTHER != null ? readContentsAsString(blobStore.get(OTHER)) : "");
                String contents = "<<<<<<< HEAD\n" +
                        headContents +
                        "=======\n" +
                        otherContents +
                        ">>>>>>>\n";
                workingArea.saveFile(contents, fileName);
                stagingArea.stageForAddition(contents, fileName);
                isConflict.set(true);
            }

            if (SPLIT == null && OTHER == null && HEAD != null) {
                workingArea.saveFile(readContentsAsString(blobStore.get(HEAD)), fileName);
            }

            if (SPLIT == null && HEAD == null && OTHER != null) {
                String contents = readContentsAsString(blobStore.get(OTHER));
                workingArea.saveFile(contents, fileName);
                stagingArea.stageForAddition(readContentsAsString(blobStore.get(OTHER)), fileName);
            }

            if (SPLIT != null && SPLIT.equals(HEAD) && OTHER == null) {
                stagingArea.stageForRemoval(workingArea.getFile(fileName));
                workingArea.deleteFile(fileName);
            }

            if (SPLIT != null && SPLIT.equals(OTHER) && HEAD == null) {
                /* leave the file removed */
            }
        });

        String commitMessage = String.format(
                "Merged %s into %s.",
                targetBranch.getName(),
                currentBranch.getName());
        commit(commitMessage, OTHER_COMMIT.getHash());

        if (isConflict.get()) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static Commit splitPoint(Branch a, Branch b) {
        Commit A = commitStore.getCommitByHash(a.getCommitHash());
        Commit B = commitStore.getCommitByHash(b.getCommitHash());
        Set<String> set = getCommitTree(A).stream().map(Commit::getHash).collect(Collectors.toSet());
        return getCommitTree(B).stream()
                .filter(commit -> set.contains(commit.getHash()))
                .findFirst()
                .orElse(null);
    }

    private static Commit getCurrentCommit() {
        String commitHash = getCurrentBranch().getCommitHash();
        return commitStore.getCommitByHash(commitHash);
    }

    private static List<Commit> getCommitChain(Commit startingCommit) {
        List<Commit> commits = new ArrayList<>();
        Commit currentCommit = startingCommit;
        while (currentCommit != null) {
            commits.add(currentCommit);
            currentCommit = commitStore.getCommitByHash(currentCommit.getParent());
        }
        return commits;
    }

    private static List<Commit> getCommitTree(Commit rootCommit) {
        List<Commit> result = new ArrayList<>();
        DFS(rootCommit, new HashSet<>(), result);
        return result;
    }

    private static void DFS(Commit node, Set<String> visited, List<Commit> list) {
        list.add(node);
        visited.add(node.getHash());

        String primaryParent = node.getParent();
        String secondaryParent = node.getSecondaryParent();

        if (primaryParent != null && !visited.contains(primaryParent)) {
            DFS(commitStore.getCommitByHash(primaryParent), visited, list);
        }

        if (secondaryParent != null && !visited.contains(secondaryParent)) {
            DFS(commitStore.getCommitByHash(secondaryParent), visited, list);
        }
    }

    private static Branch getCurrentBranch() {
        return branchStore.getBranch(head.get());
    }

    private static void setCurrentBranch(Branch branch) {
        head.set(branch);
    }

    private static void checkInitializedGitletDirectory() {
        if (!GITLET_DIR.exists()) {
            exitWithMessage("Not in an initialized Gitlet directory.");
        }
    }
}
