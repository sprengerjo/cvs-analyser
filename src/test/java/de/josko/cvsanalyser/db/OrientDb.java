package de.josko.cvsanalyser.db;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.junit.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import static java.nio.file.Files.*;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class OrientDb {

    protected final static Logger LOG = Logger.getLogger("test");

    private final static String DB_DIR = "./target/db/test";

    private static OrientGraphFactory factory;

    protected OrientGraph oGraph;

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
    }

    @AfterClass
    public static void teardownClass() {
        if (factory != null) {
            factory.close();
        }
    }

    @Before
    public void setup() throws IOException {
        oGraph = factory.getTx();
        createData();
    }

    @After
    public void teardown() {
        if (oGraph != null) {
            oGraph.drop();
            oGraph.shutdown();
        }
    }

    private Iterable<Vertex> createData() {
        Iterable<Vertex> committers = oGraph.executeOutsideTx(iArgument -> {
            oGraph.createVertexType("Committer");
            oGraph.createVertexType("Class");
            oGraph.createEdgeType("CommittedTo");

            Vertex vClass = oGraph.addVertex("class:Class");

            Vertex vPerson1 = oGraph.addVertex("class:Committer");
            Vertex vPerson2 = oGraph.addVertex("class:Committer");
            vPerson1.setProperty("firstName", "Jonas");
            vPerson2.setProperty("firstName", "Stephan");

            oGraph.addEdge("class:CommittedTo", vPerson1, vClass, "committedTo");
            oGraph.addEdge("class:CommittedTo", vPerson2, vClass, "committedTo");

            return oGraph.getVerticesOfClass("Committer");
        });
        return committers;
    }

    @Test
    public void committersWorkingOnTheSameClass() {
        Iterable<Edge> committedTos = oGraph.getEdgesOfClass("CommittedTo");
        committedTos.forEach(edge -> LOG.info(edge.toString()));
        assertThat(committedTos, not(emptyIterable()));
    }

    @Test
    public void classMustHave2Committters() {
        Iterable<Vertex> committers = oGraph.getVerticesOfClass("Committer");
        committers.forEach(committer -> LOG.info(committer.getProperty("firstName").toString()));
        assertThat(committers, not(emptyIterable()));
    }

}
