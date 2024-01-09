package gitlet;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gitlet.Utils.*;

public class StagingArea {
    private final File ADDITION_DIR;
    private final File REMOVAL_DIR;

    public StagingArea(File additionDirectory, File removalDirectory) {
        ADDITION_DIR = additionDirectory;
        REMOVAL_DIR = removalDirectory;
    }

    public File stageForAddition(File file) {
        return stageForAddition(readContentsAsString(file), file.getName());
    }

    /** Useful when adding a blob to the staging area */
    public File stageForAddition(String contents, String fileName) {
        File stagedFile = join(ADDITION_DIR, fileName);
        writeContents(stagedFile, contents);
        return stagedFile;
    }

    public File getFileForAddition(String fileName) {
        File file = join(ADDITION_DIR, fileName);
        return (file.exists()) ? file : null;
    }

    public List<File> getFilesForAddition() {
        return Objects.requireNonNull(plainFilenamesIn(ADDITION_DIR))
                .stream()
                .map(fileName -> join(ADDITION_DIR, fileName))
                .collect(Collectors.toList());
    }

    public boolean unstageForAddition(String fileName) {
        File file = getFileForAddition(fileName);
        if (!file.exists()) return false;
        return file.delete();
    }

    public File stageForRemoval(File file) {
        return stageForRemoval(readContentsAsString(file), file.getName());
    }

    /** Useful when adding a blob to the staging area */
    public File stageForRemoval(String contents, String newName) {
        File stagedFile = join(REMOVAL_DIR, newName);
        writeContents(stagedFile, contents);
        return stagedFile;
    }

    public File getFileForRemoval(String fileName) {
        File file = join(REMOVAL_DIR, fileName);
        return (file.exists()) ? file : null;
    }

    public List<File> getFilesForRemoval() {
        return Objects.requireNonNull(plainFilenamesIn(REMOVAL_DIR))
                .stream()
                .map(fileName -> join(REMOVAL_DIR, fileName))
                .collect(Collectors.toList());
    }

    public boolean unstageForRemoval(String fileName) {
        File file = getFileForRemoval(fileName);
        if (file == null) return false;
        return file.delete();
    }

    public List<File> getFiles() {
        return Stream
                .concat(
                        getFilesForAddition().stream(),
                        getFilesForRemoval().stream())
                .collect(Collectors.toList());
    }

    public void clear() {
        getFiles().forEach(File::delete);
    }

    public boolean contains(String fileName) {
        return isStagedForAddition(fileName) || isStagedForRemoval(fileName);
    }

    public boolean isStagedForAddition(String fileName) {
        return getFileForAddition(fileName) != null;
    }

    public boolean isStagedForRemoval(String fileName) {
        return getFileForRemoval(fileName) != null;
    }

    public boolean isEmpty() {
        return getFiles().isEmpty();
    }
}
