package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable {

    /** Commit message. */
    private String _message;

    /** The timestamp of the commit. */
    private String _timestamp;

    /** The parent of the commit. */
    private String _parent;

    /** The second parent of the commit. */
    private String _parent2 = "";

    /** The contents of the commit. */
    private TreeMap<String, String> _blob;

    /** The hash of the commit. */
    private String _commitHash;

    /** The time of the commit unformatted. */
    private Date _unformattedTime;

    /** Branches of a current commit. */
    private String _branches = "";

    public Commit(String message, String parent, TreeMap<String, String> map) {
        this._message = message;
        this._parent = parent;
        if (this._parent == null) {
            Date time2 = new Date(0);
            _unformattedTime = time2;
            SimpleDateFormat format =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            this._timestamp = format.format(time2);
        } else {
            Date time2 = new Date();
            _unformattedTime = time2;
            SimpleDateFormat format =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            this._timestamp = format.format(time2);
        }
        _commitHash = Utils.sha1(message, _timestamp);
        _blob = map;
    }

    public String getMessage() {
        return this._message;
    }

    public String getTimestamp() {
        return this._timestamp;
    }

    public String getParent() {
        return this._parent;
    }

    public String getParent2() {
        return _parent2;
    }

    public void setParent2(String parent2) {
        _parent2 = parent2;
    }

    public TreeMap<String, String> getBlob() {
        return _blob;
    }

    public String getHashCode() {
        return _commitHash;
    }


    public void setSplitBranches(String branch) {
        _branches += ", " + branch;
    }

    public String getSplitBranches() {
        return _branches;
    }

    public Date getUnformattedTime() {
        return _unformattedTime;
    }
}
