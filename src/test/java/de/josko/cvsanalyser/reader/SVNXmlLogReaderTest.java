package de.josko.cvsanalyser.reader;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SVNXmlLogReaderTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private LogReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new SVNXmlLogReader(this.getClass().getResourceAsStream("log.xml"));
    }

    @Test
    public void commitObjectIsFilledAsExpected() throws Exception {
        List<Commit> commits = reader.getCommits();
        Commit commit = commits.get(0);

        assertThat(commits.size(), is(2));
        assertThat(commit.getCommitter(), is("ggregory"));
        assertThat(commit.getRevision(), is("1674710"));
        assertThat(commit.getDate().toString(DATE_TIME_FORMATTER), is("2015-04-20 00:25:55"));
        assertThat(commit.getAffectedFiles().size(), is(1));
    }
}