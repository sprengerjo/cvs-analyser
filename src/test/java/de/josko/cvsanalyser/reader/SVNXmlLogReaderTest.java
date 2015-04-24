package de.josko.cvsanalyser.reader;

import de.josko.cvsanalyser.Commit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SVNXmlLogReaderTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private SVNXmlLogReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new SVNXmlLogReader(this.getClass().getResourceAsStream("log.xml"));
    }

    @Test
    public void authorsSetSizeIs7() throws Exception {
        Set<String> authors = reader.getAuthors();
        assertThat(authors.size(), is(2));
    }

    @Test
    public void getAffectedFilesReturnsFileEntriesOnly() throws Exception {
        Set<String> files = reader.getAffectedFiles();
        assertThat(files.size(), is(2));
    }

    @Test
    public void getAllRevisionDates() throws Exception {
        Set<DateTime> dates = reader.getDates();
        assertThat(dates.size(), is(2));

        Iterator<DateTime> iterator = dates.iterator();
        assertThat(iterator.next().toString(DATE_TIME_FORMATTER), is("2015-04-14 16:10:27"));
        assertThat(iterator.next().toString(DATE_TIME_FORMATTER), is("2015-04-20 00:25:55"));
    }

    @Test
    public void commitObjectIsFilledAsExpected() throws Exception {
        List<Commit> commits = reader.getCommits();
        Commit commit = commits.get(0);

        assertThat(commits.size(), is(2));
        assertThat(commit.getAuthor(), is("ggregory"));
        assertThat(commit.getRevision(), is("1674710"));
        assertThat(commit.getDate().toString(DATE_TIME_FORMATTER), is("2015-04-20 00:25:55"));
        assertThat(commit.getAffectedFiles().size(), is(1));
    }
}