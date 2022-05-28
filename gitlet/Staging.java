package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;
public class Staging implements Serializable {

    /** Contains added files. */
    private TreeMap<String, byte[]> _addedFiles;

    /** Contains removed files. */
    private ArrayList<String> _removedFiles;

    public Staging() {
        _addedFiles = new TreeMap<>();
        _removedFiles = new ArrayList<>();
    }

    public void addFile(String file, byte[] fileContent) {
        _addedFiles.put(file, fileContent);
    }

    public void removeStaged(String file) {
        _addedFiles.remove(file);
    }

    public void removeTracked(String file) {
        removeStaged(file);
        if (!_removedFiles.contains(file)) {
            _removedFiles.add(file);
        }
    }

    public void removeFromTracked(String file) {
        _removedFiles.remove(file);
    }

    public TreeMap<String, byte[]> addedFiles() {
        return _addedFiles;
    }

    public ArrayList<String> removedFiles() {
        return _removedFiles;
    }
}
