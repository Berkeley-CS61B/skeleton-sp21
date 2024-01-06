package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author xUser5000
 */
public class Commit implements Serializable, Dumpable {
    private Date timestamp;
    private String message;

    private String parent;
    private String secondaryParent;
    private List<String> trackedFiles;
    private String hash;

    public Commit(String message, List<String> trackedFiles) {
        this.message = message;
        this.trackedFiles = trackedFiles;
        this.timestamp = new Date();
        this.hash = generateHash();
    }

    public Commit(String message, List<String> trackedFiles, String leftParent) {
        this.message = message;
        this.parent = leftParent;
        this.trackedFiles = trackedFiles;
        this.timestamp = new Date();
        this.hash = generateHash();
    }

    public Commit(String message, List<String> trackedFiles, String parent, String secondaryParent) {
        this.message = message;
        this.parent = parent;
        this.secondaryParent = secondaryParent;
        this.trackedFiles = trackedFiles;
        this.timestamp = new Date();
        this.hash = generateHash();
    }

    public Commit(String message, List<String> trackedFiles, String parent, String secondaryParent, Date timestamp) {
        this.message = message;
        this.parent = parent;
        this.secondaryParent = secondaryParent;
        this.trackedFiles = trackedFiles;
        this.timestamp = timestamp;
        this.hash = generateHash();
    }

    private String generateHash() {
        List<Object> hashItems = new ArrayList<>();
        hashItems.add(timestamp.toString());
        hashItems.add(message);
        hashItems.add((parent == null) ? "" : parent);
        hashItems.add((secondaryParent == null) ? "" : secondaryParent);
        for (String file: trackedFiles) {
            hashItems.add(file);
        }
        return sha1(hashItems);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getParent() {
        return parent;
    }

    public String getSecondaryParent() {
        return secondaryParent;
    }

    public List<String> getTrackedFiles() {
        return trackedFiles;
    }

    public String getHash() {
        return hash;
    }

    public void saveCommit(File commitsDirectory) {
        File commitFile = join(commitsDirectory, hash);
        writeObject(commitFile, this);
    }

    @Override
    public String toString() {
        return "Commit{" +
                "timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", parent='" + parent + '\'' +
                ", secondaryParent='" + secondaryParent + '\'' +
                ", trackedFiles=" + trackedFiles +
                ", hash='" + hash + '\'' +
                '}';
    }

    @Override
    public void dump() {
        System.out.println(this);
    }

    public static Commit fromFile(File commitsDirectory, String commitHash) {
        File commitFile = join(commitsDirectory, commitHash);
        return readObject(commitFile, Commit.class);
    }

}
