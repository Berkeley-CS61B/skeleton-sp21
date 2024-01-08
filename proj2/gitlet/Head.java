package gitlet;

import java.io.File;

import static gitlet.Utils.*;

public class Head {
    private final File HEAD_FILE;

    public Head(File headFile) {
        HEAD_FILE = headFile;
    }

    public String get() {
        return readContentsAsString(HEAD_FILE);
    }

    public void set(Branch branch) {
        writeContents(HEAD_FILE, branch.getName());
    }

}
