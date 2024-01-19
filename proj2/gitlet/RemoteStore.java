package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

public class RemoteStore {

    private final File REMOTE_DIR;

    public RemoteStore(File REMOTE_DIR) {
        this.REMOTE_DIR = REMOTE_DIR;
        if (!REMOTE_DIR.exists()) {
            REMOTE_DIR.mkdir();
        }
    }

    public String getRemoteURI(String remoteName) {
        File remoteFile = join(REMOTE_DIR, remoteName);
        return (remoteFile.exists()) ? readContentsAsString(remoteFile) : null;
    }

    public boolean contains(String remoteName) {
        return getRemoteURI(remoteName) != null;
    }

    public void addRemote(String name, String URI) throws IOException {
        File remoteFile = join(REMOTE_DIR, name);
        if (remoteFile.exists()) {
            exitWithMessage("A remote with that name already exists.");
        }
        remoteFile.createNewFile();
        writeContents(remoteFile, URI);
    }

    public void removeRemote(String remoteName) {
        File remoteFile = join(REMOTE_DIR, remoteName);
        if (!remoteFile.exists()) {
            exitWithMessage("A remote with that name does not exist.");
        }
        remoteFile.delete();
    }
}
