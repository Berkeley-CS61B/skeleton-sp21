package gitlet;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static gitlet.Utils.*;

public class WorkingArea {
    private final File WORKING_DIR;

    public WorkingArea(File WORKING_DIR) {
        this.WORKING_DIR = WORKING_DIR;
    }

    public File getFile(String fileName) {
        File file =  join(WORKING_DIR, fileName);
        return file.exists() ? file : null;
    }

    public File saveFile(String contents, String fileName) {
        File file = join(WORKING_DIR, fileName);
        writeContents(file, contents);
        return file;
    }

    public boolean deleteFile(String fileName) {
        File file = getFile(fileName);
        if (file == null) return false;
        return file.delete();
    }

    public List<File> allFiles() {
        return Objects.requireNonNull(plainFilenamesIn(WORKING_DIR))
                .stream()
                .map(fileName -> join(WORKING_DIR, fileName))
                .collect(Collectors.toList());
    }
}
