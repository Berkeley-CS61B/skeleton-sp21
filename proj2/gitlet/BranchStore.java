package gitlet;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static gitlet.Utils.*;

public class BranchStore {
    private final File BRANCHES_DIR;

    public BranchStore(File branchDirectory) {
        BRANCHES_DIR = branchDirectory;
    }

    public Branch getBranch(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) return null;
        return readObject(branchFile, Branch.class);
    }

    public boolean containsBranch(String branchName) {
        return getBranch(branchName) != null;
    }

    public void saveBranch(Branch branch) {
        File branchFile = join(BRANCHES_DIR, branch.getName());
        writeObject(branchFile, branch);
    }

    public void removeBranch(Branch branch) {
        File branchFile = join(BRANCHES_DIR, branch.getName());
        if (branchFile.exists()) branchFile.delete();
    }

    public List<Branch> allBranches() {
        return Objects.requireNonNull(plainFilenamesIn(BRANCHES_DIR))
                .stream()
                .map(this::getBranch)
                .collect(Collectors.toList());
    }

}
