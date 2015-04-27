package de.josko.cvsanalyser;

import de.josko.cvsanalyser.db.OrientDbWriter;
import de.josko.cvsanalyser.reader.Commit;
import de.josko.cvsanalyser.reader.LogReader;
import de.josko.cvsanalyser.reader.SVNXmlLogReader;
import org.joda.time.DateTime;
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

    public void runReactivly(LogReader reader, OrientDbWriter writer) {
//FIXME: join all vertex observables into on, to make it faster
        Observable<String> authors = Observable.from(reader.getAuthors());
        authors.forEach(author -> writer.addVertex(writer.V_COMMITTER, author));

        Observable<String> revisions = Observable.from(reader.getRevisions());
        revisions.forEach(revision -> writer.addVertex(writer.V_REVISION, revision));

        Observable<DateTime> dates = Observable.from(reader.getDates());
        dates.forEach(date -> writer.addVertex(writer.V_DATE, date));

        Observable<String> files = Observable.from(reader.getAffectedFiles());
        files.forEach(file -> writer.addVertex(writer.V_CLASS, file));


        Observable<Commit> commits = Observable.from(reader.getCommits());
        commits.forEach(commit -> writer.commits(commit));
    }
}
