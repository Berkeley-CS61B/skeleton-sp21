package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Branch implements Serializable, Dumpable {
    private String name;

    /** hash of the commit pointed to by this branch */
    private String head;

    public Branch(String name, String head) {
        this.name = name;
        this.head = head;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public void saveBranch(File branchesDirectory) {
        File branchFile = join(branchesDirectory, name);
        writeObject(branchFile, this);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "name='" + name + '\'' +
                ", head='" + head + '\'' +
                '}';
    }

    @Override
    public void dump() {
        System.out.println(this);
    }

    public static Branch fromFile(File branchesDirectory, String branchName) {
        File branchFile = join(branchesDirectory, branchName);
        return readObject(branchFile, Branch.class);
    }

}
