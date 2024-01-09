package gitlet;

import java.io.Serializable;

public class Branch implements Serializable, Dumpable {
    private String name;
    private String commitHash;

    public Branch(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommit(Commit commit) {
        this.commitHash = commit.getHash();
    }

    public void setCommit(String commitHash) {
        this.commitHash = commitHash;
    }

    @Override
    public String toString() {
        return "Branch{" +
                "name='" + name + '\'' +
                ", head='" + commitHash + '\'' +
                '}';
    }

    @Override
    public void dump() {
        System.out.println(this);
    }

}
