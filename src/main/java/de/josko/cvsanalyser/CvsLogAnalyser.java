package de.josko.cvsanalyser;

import de.josko.cvsanalyser.db.OrientDbWriter;
import de.josko.cvsanalyser.reader.LogReader;
import de.josko.cvsanalyser.reader.SVNXmlLogReader;
import rx.Observable;


public class CvsLogAnalyser {

    public static void main(String[] args) {
        CvsLogAnalyser cvsLogAnalyser = new CvsLogAnalyser();

        LogReader reader = new SVNXmlLogReader(CvsLogAnalyser.class.getResourceAsStream("log.xml"));
        OrientDbWriter writer = new OrientDbWriter();

        cvsLogAnalyser.run(reader, writer);
        writer.shutDown();
    }

    public void run(LogReader reader, OrientDbWriter writer) {
        reader.getAuthors().forEach(author -> writer.committers(author));
        reader.getRevisions().forEach(revision -> writer.revisions(revision));
        reader.getDates().forEach(date -> writer.dates(date));
        reader.getAffectedFiles().forEach(file -> writer.files(file));
        reader.getCommits().forEach(commit -> writer.commits(commit));
    }
}
