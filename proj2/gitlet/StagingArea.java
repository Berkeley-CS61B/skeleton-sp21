package gitlet;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static gitlet.Utils.*;

public class StagingArea {
    private final File ADDITION_DIR;
    private final File REMOVAL_DIR;

    public StagingArea(File additionDirectory, File removalDirectory) {
        ADDITION_DIR = additionDirectory;
        REMOVAL_DIR = removalDirectory;
    }

    public File stageFileForAddition(File file) {
        return stageFileForAddition(file, file.getName());
    }

    /** Useful when adding a blob to the staging area */
    public File stageFileForAddition(File file, String newName) {
        File stagedFile = join(ADDITION_DIR, newName);
        writeContents(stagedFile, readContentsAsString(file));
        return stagedFile;
    }

    public File getFileForAddition(String fileName) {
        File file = join(ADDITION_DIR, fileName);
        return (file.exists()) ? file : null;
    }

    public File stageFileForRemoval(File file) {
        return stageFileForRemoval(file, file.getName());
    }

    /** Useful when adding a blob to the staging area */
    public File stageFileForRemoval(File file, String newName) {
        File stagedFile = join(REMOVAL_DIR, newName);
        writeContents(stagedFile, readContentsAsString(file));
        return stagedFile;
    }

    public File getFileForRemoval(String fileName) {
        File file = join(REMOVAL_DIR, fileName);
        return (file.exists()) ? file : null;
    }

    public List<File> getFilesForAddition() {
        return Objects.requireNonNull(plainFilenamesIn(ADDITION_DIR))
                .stream()
                .map(fileName -> join(ADDITION_DIR, fileName))
                .collect(Collectors.toList());
    }

    public List<File> getFilesForRemoval() {
        return Objects.requireNonNull(plainFilenamesIn(REMOVAL_DIR))
                .stream()
                .map(fileName -> join(REMOVAL_DIR, fileName))
                .collect(Collectors.toList());
    }

    public List<File> getFiles() {
        List<File> addedFiles = getFilesForAddition();
        addedFiles.addAll(getFilesForRemoval());
        return addedFiles;
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
