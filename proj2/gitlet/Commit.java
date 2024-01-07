package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  @author xUser5000
 */
public class Commit implements Serializable, Dumpable {
    private final String message;
    private final Date timestamp;
    /** Original parent from the  */
    private final String parent;
    private final String secondaryParent;

    private final Map<String, String> trackedFiles;
    private final String hash;

    public Commit(String message, Date timestamp, Map<String, String> trackedFiles, String parent, String secondaryParent) {
        this.message = message;
        this.timestamp = (timestamp != null) ? timestamp : new Date();
        this.parent = parent;
        this.secondaryParent = secondaryParent;
        this.trackedFiles = (trackedFiles != null) ? trackedFiles : new TreeMap<>();
        this.hash = generateHash();
    }

    private String generateHash() {
        List<Object> hashItems = new ArrayList<>();
        hashItems.add(message);
        hashItems.add(timestamp.toString());
        hashItems.add((parent == null) ? "" : parent);
        hashItems.add((secondaryParent == null) ? "" : secondaryParent);
        for (Map.Entry<String, String> entry: trackedFiles.entrySet()) {
            hashItems.add(entry.toString());
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

    public Map<String, String> getTrackedFiles() {
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

    public String log() {
        StringBuilder builder = new StringBuilder();
        builder.append("===\n");
        builder.append(String.format("commit %s\n", getHash()));
        if (getParent() != null && getSecondaryParent() != null) {
            builder.append(
                    String.format("Merge: %s %s\n",  getParent().substring(0, 7),  getSecondaryParent().substring(0, 7))
            );
        }
        Formatter formatter = new Formatter().format("Date: %1$ta %1$tb %1$td %1$tT %1$tY %1$tz", getTimestamp());
        String formattedDate = formatter.toString();
        builder.append(String.format("%s\n", formattedDate));
        builder.append(String.format("%s\n\n", getMessage()));
        return builder.toString();
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
