package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

public class Obj implements Serializable {

    /** Current working directory. */
    private final String _CWD = "./";
    /** Gitlet directory. */
    private final String _GITLETDIR = ".gitlet/";
    /** Commit directory. */
    private final String _COMMITDIR = ".gitlet/commits/";
    /** Blob directory. */
    private final String _BLOBDIR = ".gitlet/blobs/";

    /** Current working directory2. */
    private File _currentWD;
    /** All the hashes of the commits. */
    private ArrayList<String> _allCommits = new ArrayList<>();
    /** All of the branches. */
    private TreeMap<String, String> _allBranches = new TreeMap<>();
    /** The name of the current branch. */
    private String _currBranch;
    /** The hash of the head commit. */
    private String _headPointer;
    /** A new staging area. */
    private Staging _stage = new Staging();
    /** All of the current splits. */
    private TreeMap<String, Integer> _allCurrsplits = new TreeMap<>();
    /** The remote repository. */
    private TreeMap<String, String> _remote = new TreeMap<>();

    public void init() throws IOException {
        File original = new File(_GITLETDIR);
        if (original.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else {
            _currentWD = new File(System.getProperty("user.dir"));
            original.mkdir();
            Files.createDirectory(Paths.get(_COMMITDIR));
            Files.createDirectory(Paths.get(_BLOBDIR));

            Commit initCommit = new Commit("initial commit",
                    null, new TreeMap<String, String>());
            _allCommits.add(initCommit.getHashCode());
            _headPointer = initCommit.getHashCode();
            _allBranches.put("master", initCommit.getHashCode());
            _currBranch = "master";
            File fileTemp = Utils.join(_COMMITDIR, initCommit.getHashCode());
            Utils.writeObject(fileTemp, initCommit);
        }
    }


    public void add(String file) {
        if (!new File(file).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            byte[] fileContent = Utils.readContents(new File(file));
            String fileHash = Utils.sha1(fileContent);
            Commit headCommit =
                    Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                            Commit.class);
            if (headCommit.getBlob().containsKey(file)
                    && fileHash.equals(headCommit.getBlob().get(file))) {
                _stage.removeStaged(file);
            } else {
                _stage.addFile(file, fileContent);
            }
            _stage.removeFromTracked(file);
        }
    }

    public void commit(String commitMsg) {
        if (commitMsg.equals("")) {
            System.out.println("Please enter a commit message.");
        } else if (_stage.addedFiles().isEmpty()
                && _stage.removedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            TreeMap<String, String> commitfiles = new TreeMap<>();

            Commit parentCommit =
                    Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                            Commit.class);
            for (String file : parentCommit.getBlob().keySet()) {
                if (!_stage.removedFiles().contains(file)) {
                    commitfiles.put(file, parentCommit.getBlob().get(file));
                }
            }

            for (String file : _stage.addedFiles().keySet()) {
                commitfiles.put(file,
                        Utils.sha1(_stage.addedFiles().get(file)));
                Utils.writeContents(Utils.join(_BLOBDIR,
                        Utils.sha1(_stage.addedFiles().get(file))),
                        _stage.addedFiles().get(file));
            }
            _stage = new Staging();
            Commit newCom = new Commit(commitMsg, _headPointer, commitfiles);
            _headPointer = newCom.getHashCode();
            _allCommits.add(_headPointer);
            Utils.writeObject(Utils.join(_COMMITDIR, _headPointer), newCom);
            _allBranches.put(_currBranch, _headPointer);
        }
    }

    public void log() {
        Commit recentCom =
                Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                        Commit.class);
        while (recentCom.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + recentCom.getHashCode());
            if (!recentCom.getParent2().equals("")) {
                System.out.println("Merge: "
                        + recentCom.getParent().substring(0, 7)
                        + " " + recentCom.getParent2().substring(0, 7));
            }
            System.out.println("Date: " + recentCom.getTimestamp());
            System.out.println(recentCom.getMessage());
            System.out.println();
            recentCom = Utils.readObject(Utils.join(_COMMITDIR,
                    recentCom.getParent()), Commit.class);

        }
        System.out.println("===");
        System.out.println("commit " + recentCom.getHashCode());
        System.out.println("Date: " + recentCom.getTimestamp());
        System.out.println(recentCom.getMessage());
    }

    public void checkoutFile(String file) {
        checkoutCommit(file, _headPointer);
    }

    public void checkoutCommit(String file, String commitid) {
        String existingCommitID = "";
        for (String commitHash : _allCommits) {
            if (commitHash.startsWith(commitid)) {
                existingCommitID = commitHash;
            }
        }

        if (existingCommitID.equals("")) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit head =
                    Utils.readObject(Utils.join(_COMMITDIR, existingCommitID),
                            Commit.class);
            if (head.getBlob().get(file) == null) {
                System.out.println("File does not exist in that commit.");
            } else {
                Utils.writeContents(new File(file),
                        Utils.readContents(Utils.join(_BLOBDIR,
                                head.getBlob().get(file))));
            }
        }
    }


    public void checkoutBranch(String branchName) {
        String commitID = _allBranches.get(branchName);

        if (!untrackedFiles().isEmpty()) {
            Commit branchCommit =
                    Utils.readObject(new File(_COMMITDIR + commitID),
                            Commit.class);
            for (String file : untrackedFiles()) {
                if (Utils.plainFilenamesIn(new File(_CWD)).contains(file)
                        && branchCommit.getBlob().containsKey(file)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        if (commitID == null) {
            System.out.println("No such branch exists.");
        } else if (_currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit branchCommit =
                    Utils.readObject(Utils.join(_COMMITDIR, commitID),
                            Commit.class);

            for (String file : branchCommit.getBlob().keySet()) {
                String blobHash = branchCommit.getBlob().get(file);
                Utils.writeContents(new File(file),
                        Utils.readContents(Utils.join(_BLOBDIR, blobHash)));
            }

            for (String file : Utils.plainFilenamesIn(new File(_CWD))) {
                if (!branchCommit.getBlob().containsKey(file)) {
                    if (Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                            Commit.class).getBlob().containsKey(file)) {
                        Utils.restrictedDelete(new File(file));
                    }
                }
            }
            _stage = new Staging();
            _currBranch = branchName;
            _headPointer = commitID;

        }
    }

    public void rm(String file) {
        Commit rmCommit =
                Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                        Commit.class);
        if (!rmCommit.getBlob().containsKey(file)
                && !_stage.addedFiles().containsKey(file)) {
            System.out.println("No reason to remove the file.");
        } else {
            if (_stage.addedFiles().containsKey(file)) {
                _stage.removeStaged(file);
            }
            if (rmCommit.getBlob().containsKey(file)) {
                if (Utils.plainFilenamesIn(new File(_CWD)).contains(file)) {
                    Utils.restrictedDelete(new File(file));
                }
                _stage.removeTracked(file);
            }
        }
    }

    public void globalLog() {
        for (String file : _allCommits) {
            Commit recentCom =
                    Utils.readObject(Utils.join(_COMMITDIR, file),
                            Commit.class);
            System.out.println("===");
            System.out.println("commit " + recentCom.getHashCode());
            if (!recentCom.getParent2().equals("")) {
                System.out.println("Merge: "
                        + recentCom.getParent().substring(0, 7)
                        + recentCom.getParent2().substring(0, 7));
            }
            System.out.println("Date: " + recentCom.getTimestamp());
            System.out.println(recentCom.getMessage());
            System.out.println();
        }
    }

    public void find(String commitMsg) {
        boolean commitFound = false;
        for (String file : _allCommits) {
            Commit recentCom =
                    Utils.readObject(Utils.join(_COMMITDIR, file),
                            Commit.class);
            if (recentCom.getMessage().startsWith(commitMsg)) {
                System.out.println(file);
                commitFound = true;
            }
        }
        if (!commitFound) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void branch(String branchName) {
        if (_allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            _allBranches.put(branchName, _headPointer);
            Commit headCommit =
                    Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                            Commit.class);
            Utils.writeObject(Utils.join(_COMMITDIR, _headPointer),
                    headCommit);
        }
    }

    public void rmBranch(String branchName) {
        if (!_allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (_currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        String headpoint = _allBranches.get(branchName);
        Commit headCommit =
                Utils.readObject(Utils.join(_COMMITDIR, headpoint),
                        Commit.class);
        Utils.writeObject(Utils.join(_COMMITDIR, headpoint), headCommit);

        _allBranches.remove(branchName);
    }

    public void status() {

        System.out.println("=== Branches ===");
        for (String branch : _allBranches.keySet()) {
            if (_currBranch.equals(branch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String file : _stage.addedFiles().keySet()) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String file : _stage.removedFiles()) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");

        modifiedFiles();

        System.out.println("=== Untracked Files ===");

        for (String file : untrackedFiles()) {
            System.out.println(file);
        }
        System.out.println();
    }


    public ArrayList<String> untrackedFiles() {
        ArrayList<String> allFiles = new ArrayList<>();
        for (String file : Utils.plainFilenamesIn(new File(_CWD))) {
            if (!Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                    Commit.class).getBlob().containsKey(file)
                    && !_stage.addedFiles().keySet().contains(file)) {
                allFiles.add(file);
            }
        }
        return allFiles;
    }


    public void modifiedFiles() {
        TreeMap<String, String>  allFiles = new TreeMap<>();
        Commit currCommit =
                Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                        Commit.class);
        for (String file : currCommit.getBlob().keySet()) {
            if (!Utils.plainFilenamesIn(new File(_CWD)).contains(file)
                    && !_stage.removedFiles().contains(file)) {
                allFiles.put(file, "deleted");
            }
        }

        for (String file : Utils.plainFilenamesIn(new File(_CWD))) {
            if (currCommit.getBlob().containsKey(file)
                    && !currCommit.getBlob().get(file).equals
                    (Utils.sha1(Utils.readContents(Utils.join(_CWD, file))))) {
                allFiles.put(file, "modified");
            }
        }
        for (String fileName : allFiles.keySet()) {
            System.out.println(fileName + "(" + allFiles.get(fileName) + ")");
        }

        System.out.println();
    }

    public void reset(String commitID) {
        String existingCommitID = "";
        for (String commitHash : _allCommits) {
            if (commitHash.startsWith(commitID)) {
                existingCommitID = commitHash;
            }
        }

        if (!untrackedFiles().isEmpty()) {
            Commit branchCommit =
                    Utils.readObject(new File(_COMMITDIR + commitID),
                            Commit.class);
            for (String file : untrackedFiles()) {
                if (Utils.plainFilenamesIn(new File(_CWD)).contains(file)
                        && branchCommit.getBlob().containsKey(file)) {
                    System.out.println(
                            "There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        if (existingCommitID.equals("")) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit resetCommit =
                    Utils.readObject(Utils.join(_COMMITDIR, existingCommitID),
                            Commit.class);
            Commit currentCommit =
                    Utils.readObject(Utils.join(_COMMITDIR, _headPointer),
                            Commit.class);

            for (String file : resetCommit.getBlob().keySet()) {
                if (!(resetCommit.getBlob().get(file)).equals(
                        currentCommit.getBlob().get(file))) {
                    byte[] blobContent =
                            Utils.readContents(Utils.join(_BLOBDIR,
                                    resetCommit.getBlob().get(file)));
                    Utils.writeContents(Utils.join(_CWD, file), blobContent);
                }
            }

            for (String file : currentCommit.getBlob().keySet()) {
                if (!resetCommit.getBlob().containsKey(file)) {
                    Utils.restrictedDelete(Utils.join(_CWD, file));
                }
            }
            _stage = new Staging();
            _allBranches.put(_currBranch, existingCommitID);
            _headPointer = existingCommitID;
        }
    }

    public TreeMap<String, Integer> findAllSplits(String tempStr,
                                                  int distance) {
        Commit prevCommit;
        Commit tempCommit = Utils.readObject(new File(_COMMITDIR + tempStr),
                Commit.class);
        if (tempCommit.getParent() == null) {
            _allCurrsplits.put(tempStr, distance);
            return _allCurrsplits;
        }
        if (tempCommit.getParent() != null) {
            prevCommit = Utils.readObject(new File(_COMMITDIR
                    + tempCommit.getParent()), Commit.class);
            _allCurrsplits.put(tempStr, distance);
            findAllSplits(prevCommit.getHashCode(), distance + 1);
        }
        if (!tempCommit.getParent2().equals("")) {
            prevCommit = Utils.readObject(new File(_COMMITDIR
                    + tempCommit.getParent2()), Commit.class);
            _allCurrsplits.put(tempStr, distance);
            findAllSplits(prevCommit.getHashCode(), distance + 1);
        }

        return _allCurrsplits;
    }

    public String findSplit(String otherName) {
        Commit headCommit = Utils.readObject(new File(_COMMITDIR
                + _headPointer), Commit.class);
        Commit otherCommit = Utils.readObject(new File(_COMMITDIR
                + _allBranches.get(otherName)), Commit.class);
        TreeMap<String, Integer> currSplits =
                (TreeMap<String, Integer>) findAllSplits(
                        headCommit.getHashCode(), 0).clone();
        _allCurrsplits.clear();
        TreeMap<String, Integer> otherSplits =
                (TreeMap<String, Integer>) findAllSplits(
                        otherCommit.getHashCode(), 0).clone();
        _allCurrsplits.clear();
        TreeMap<String, Integer> overlapSplits = new TreeMap<>();

        for (String tempHash1 : currSplits.keySet()) {
            for (String tempHash2 : otherSplits.keySet()) {
                if (tempHash1.equals(tempHash2)) {
                    overlapSplits.put(tempHash1, currSplits.get(tempHash1));
                }
            }
        }

        int shortest = Integer.MAX_VALUE;
        String shortestCommit = "";
        for (String tempHash : overlapSplits.keySet()) {
            if (overlapSplits.get(tempHash) < shortest) {
                shortest = overlapSplits.get(tempHash);
                shortestCommit = tempHash;
            }
        }
        return shortestCommit;
    }

    public boolean mergeHelper(HashSet<String> combinedFiles, Commit head,
                            Commit other, Commit split) {
        boolean isConflict = false;
        String headHash = ""; String otherHash = ""; String splitHash = "";
        for (String file : combinedFiles) {
            if (head.getBlob().get(file) != null) {
                headHash = head.getBlob().get(file);
            } else {
                headHash = "";
            }
            if (other.getBlob().get(file) != null) {
                otherHash = other.getBlob().get(file);
            } else {
                otherHash = "";
            }
            if (split.getBlob().get(file) != null) {
                splitHash = split.getBlob().get(file);
            } else {
                splitHash = "";
            }
            if (!headHash.equals(otherHash)) {
                if (splitHash.equals(headHash)) {
                    if (!otherHash.equals("")) {
                        Utils.writeContents(Utils.join(_CWD, file),
                                Utils.readContents(Utils.join(_BLOBDIR,
                                        otherHash)));
                        _stage.addFile(file,
                                Utils.readContents(Utils.join(_BLOBDIR,
                                        otherHash)));
                    } else {
                        Utils.restrictedDelete(Utils.join(_CWD, file));
                        _stage.removeTracked(file);
                    }
                } else if (splitHash.equals(otherHash)) {
                    String str = "";
                } else {
                    isConflict = true;
                    String otherstr = "";
                    String headstr = "";
                    if (!otherHash.equals("")) {
                        otherstr =
                                Utils.readContentsAsString
                                        (Utils.join(_BLOBDIR, otherHash));
                    }
                    if (!headHash.equals("")) {
                        headstr = Utils.readContentsAsString
                                (Utils.join(_BLOBDIR, headHash));
                    }
                    String combinedstr = "<<<<<<< HEAD\n" + headstr
                            + "=======\n"
                            + otherstr + ">>>>>>>\n";
                    Utils.writeContents(Utils.join(_CWD, file),
                            combinedstr);
                    _stage.addFile(file,
                            Utils.readContents(Utils.join(_CWD, file)));
                }
            }
        }
        return isConflict;
    }

    public void errorCheck(String branchName) {
        if (!untrackedFiles().isEmpty()) {
            Commit branchCommit = Utils.readObject(Utils.join(_COMMITDIR,
                    _allBranches.get(branchName)), Commit.class);
            for (String file : untrackedFiles()) {
                if (Utils.plainFilenamesIn(new File(_CWD)).contains(file)
                        && branchCommit.getBlob().containsKey(file)) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it, or add and "
                            + "commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    public void merge(String branchName) {

        errorCheck(branchName);

        if (!_stage.addedFiles().isEmpty()
                || !_stage.removedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!_allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (_currBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
        } else {
            Commit head = Utils.readObject(Utils.join(_COMMITDIR,
                    _headPointer), Commit.class);
            Commit other = Utils.readObject(Utils.join(_COMMITDIR,
                    _allBranches.get(branchName)), Commit.class);
            Commit split = Utils.readObject(Utils.join(_COMMITDIR,
                    findSplit(branchName)), Commit.class);
            if (other.getHashCode().equals(split.getHashCode())) {
                System.out.println("Given branch is an "
                        + "ancestor of the current branch.");
                System.exit(0);
            }
            if (head.getHashCode().equals(split.getHashCode())) {
                checkoutBranch(branchName);
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            }
            HashSet<String> combinedFiles = new HashSet<>();
            for (String temp : other.getBlob().keySet()) {
                combinedFiles.add(temp);
            }
            for (String temp : split.getBlob().keySet()) {
                combinedFiles.add(temp);
            }
            for (String temp : head.getBlob().keySet()) {
                combinedFiles.add(temp);
            }
            boolean isConflict = mergeHelper(combinedFiles, head, other, split);
            commit("Merged "
                    + branchName +  " into " + _currBranch + ".");
            Commit mergedCommit = Utils.readObject(Utils.join(_COMMITDIR,
                    _headPointer), Commit.class);
            mergedCommit.setParent2(other.getHashCode());
            Utils.writeObject(Utils.join(_COMMITDIR,
                    _headPointer), mergedCommit);
            if (isConflict) {
                System.out.println("Encountered a merge conflict.");
                isConflict = false;
            }
        }
    }
}
