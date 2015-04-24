package de.josko.cvsanalyser;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

public class Commit {
    private String author;
    private DateTime date;
    private Set<String> affectedFiles = new HashSet<>();
    private String revision;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
}
