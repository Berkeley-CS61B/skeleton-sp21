package gitlet;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gitlet.Utils.*;

public class CommitStore {
    private final File COMMITS_DIR;

    public CommitStore(File commitDirectory) {
        COMMITS_DIR = commitDirectory;
        if (!COMMITS_DIR.exists()) {
            COMMITS_DIR.mkdir();
        }
    }

    public Commit getCommitByHash(String commitId) {
        if (commitId == null) return null;
        File commitFile = join(COMMITS_DIR, commitId);
        if (commitFile.exists()) {
            return readObject(commitFile, Commit.class);
        }
        return allCommitsStream()
                .filter(commit -> commit.getHash().startsWith(commitId))
                .findFirst()
                .orElse(null);
    }

    public List<Commit> getCommitsByMessage(String message) {
        return allCommitsStream()
                .filter(commit -> commit.getMessage().equals(message))
                .collect(Collectors.toList());
    }

    public void saveCommit(Commit commit) {
        File commitFile = join(COMMITS_DIR, commit.getHash());
        writeObject(commitFile, commit);
    }

    public List<Commit> allCommits() {
        return allCommitsStream().collect(Collectors.toList());
    }

    public Stream<Commit> allCommitsStream() {
        return Objects.requireNonNull(plainFilenamesIn(COMMITS_DIR))
                .stream()
                .map(this::getCommitByHash);
    }
}
