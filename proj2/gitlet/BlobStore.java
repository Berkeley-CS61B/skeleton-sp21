package gitlet;

import java.io.File;

import static gitlet.Utils.*;

public class BlobStore {
    private final File BLOBS_DIR;

    public BlobStore(File blobDirectory) {
        BLOBS_DIR = blobDirectory;
    }

    public File get(String hash) {
        File file = join(BLOBS_DIR, hash);
        return (file.exists()) ? file : null;
    }

    public File save(File file) {
        String contents = readContentsAsString(file);
        String hash = sha1(contents);
        File storedBlob = join(BLOBS_DIR, hash);
        writeContents(storedBlob, contents);
        return storedBlob;
    }

    public boolean contains(String hash) {
        return get(hash) != null;
    }
}
