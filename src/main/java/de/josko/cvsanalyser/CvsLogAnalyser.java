package de.josko.cvsanalyser;

import de.josko.cvsanalyser.db.OrientDbWriter;
import de.josko.cvsanalyser.reader.Commit;
import de.josko.cvsanalyser.reader.LogReader;
import de.josko.cvsanalyser.reader.SVNXmlLogReader;
import rx.Observable;


public class CvsLogAnalyser {

    public static void main(String[] args) {
        CvsLogAnalyser cvsLogAnalyser = new CvsLogAnalyser();

        LogReader reader = new SVNXmlLogReader(CvsLogAnalyser.class.getResourceAsStream("log.gbtec.xml"));
        OrientDbWriter writer = new OrientDbWriter();

        cvsLogAnalyser.runReactivly(reader, writer);
        writer.shutDown();
    }

    public void run(LogReader reader, OrientDbWriter writer) {
        reader.getCommits().forEach(commit -> writer.processCommits(commit));
    }

    public void runReactivly(LogReader reader, OrientDbWriter writer) {
        Observable<Commit> commits = Observable.from(reader.getCommits());
        commits.forEach(commit -> writer.processCommits(commit));
    }
}
