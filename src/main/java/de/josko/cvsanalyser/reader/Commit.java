package de.josko.cvsanalyser.reader;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

public class Commit {
    private String committer;
    private DateTime date;
    private Set<String> affectedFiles = new HashSet<>();
    private String revision;
    private String message;

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public Set<String> getAffectedFiles() {
        return affectedFiles;
    }

    public void setAffectedFiles(Set<String> affectedFiles) {
        this.affectedFiles = affectedFiles;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRevision() {
        return revision;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
