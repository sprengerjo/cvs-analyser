package de.josko.cvsanalyser.db;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import de.josko.cvsanalyser.CvsLogAnalyser;
import de.josko.cvsanalyser.reader.Commit;
import de.josko.cvsanalyser.reader.LogReader;
import de.josko.cvsanalyser.reader.SVNXmlLogReader;
import org.joda.time.DateTime;
import org.junit.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;

import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: cojo
 * Date: 27.04.15
 * Time: 19:59
 * To change this template use File | Settings | File Templates.
 */
public class OrientDbWriterTest {

    private final static String DB_DIR = "./target/db/test";

    static OrientGraphFactory factory;
    static OrientDbWriter writer;

    @BeforeClass
    public static void setupClass() throws IOException {
        Path root = Paths.get(DB_DIR);
        if (exists(root)) {
            walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
        factory = new OrientGraphFactory("plocal:" + DB_DIR);
        new OrientDbWriter(factory);

    }

    @AfterClass
    public static void teardownClass() {
        writer.shutDown();
    }

    @Before
    public void setup() throws IOException {
        writer = new OrientDbWriter(factory);
    }

    @After
    public void teardown() {
        writer.destroy();
    }


    @Test
    public void createlong() throws Exception {
        long time = System.currentTimeMillis();
        LogReader reader = new SVNXmlLogReader(CvsLogAnalyser.class.getResourceAsStream("log-long.xml"));

        new CvsLogAnalyser().run(reader, writer);

        System.out.println("time elapsed: "   + (System.currentTimeMillis() - time));
    }

    @Test
    public void committerMustBeCreated() throws Exception {
        writer.committers("anything");
        writer.getGraph().getVerticesOfClass(writer.V_COMMITTER).forEach(
                actual -> assertThat(actual.getProperty(writer.V_COMMITTER).toString(), equalTo("anything")));
    }

    @Test
    public void commitsRelationMustCreated() throws Exception {
        Commit commit = setUpData();
        writer.commits(commit);

        writer.edges.stream().forEach(edge ->
                writer.getGraph().getEdgesOfClass(edge).forEach(
                actual -> assertThat(actual, notNullValue())));
    }

    private Commit setUpData() {
        DateTime date = DateTime.now();

        writer.dates(date);
        writer.committers("anything");
        writer.revisions("anything");
        writer.files("anything");

        Commit commit = new Commit();
        commit.setCommitter("anything");
        commit.setRevision("anything");
        commit.setAffectedFiles(Collections.singleton("anything"));
        commit.setMessage("anything");
        commit.setDate(date);
        return commit;
    }
}
