package de.josko.cvsanalyser.reader;

import de.josko.cvsanalyser.Commit;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface LogReader {
    Set<String> getAuthors();
    Set<DateTime> getDates();
    Set<String> getAffectedFiles();
    Set<String> getRevisions();
    List<Commit> getCommits();
}
