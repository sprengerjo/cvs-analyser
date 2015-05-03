package de.josko.cvsanalyser.reader;

import java.util.List;

public interface LogReader {
    List<Commit> getCommits();
}
