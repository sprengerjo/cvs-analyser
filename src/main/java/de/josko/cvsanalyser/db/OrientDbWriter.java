package de.josko.cvsanalyser.db;

import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import de.josko.cvsanalyser.reader.Commit;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: cojo
 * Date: 27.04.15
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
public class OrientDbWriter {

    private OrientGraphFactory factory;
    private OrientGraph oGraph;

    public static final String V_COMMITTER = "Committer";
    public static final String V_REVISION = "Revision";
    public static final String V_DATE = "Date";
    public static final String V_CLASS = "Class";

    static final String E_COMMITTED = "committed";
    static final String E_CONTAINS = "contains";
    static final String E_COMMITTED_ON = "committedOn";

    List<String> vertices = Arrays.asList(new String[]{V_COMMITTER, V_REVISION,
            V_DATE, V_CLASS});
    List<String> edges = Arrays.asList(new String[]{E_COMMITTED, E_CONTAINS,
            E_COMMITTED_ON});

    private static final String DB_DIR = "localhost/test";

    private final static Logger LOG = Logger.getLogger(OrientDbWriter.class.getSimpleName());

    OrientDbWriter(OrientGraphFactory factory) {
        this.factory = factory;
        oGraph = factory.getTx();
        setUpVertexesAndEdges();
    }

    public OrientDbWriter() {
        openConnection();
        setUpVertexesAndEdges();
    }

    private void setUpVertexesAndEdges() {
        oGraph.executeOutsideTx(iArgument -> {
            vertices.stream().forEach(v -> createVertexType(v));
            edges.stream().forEach(e -> createEdgeType(e));
            return null;
        });
    }

    public void shutDown() {
        oGraph.shutdown();
        factory.close();
    }

    void destroy() {
        oGraph.drop();
        oGraph.shutdown();
    }

    private void openConnection() {
        factory = new OrientGraphFactory("remote:" + DB_DIR, "root", "root");
        oGraph = factory.getTx();
    }

    public void addVertex(String className, Object value) {
        Vertex vertex = getVertex(className, value);
        if (vertex == null) {
            LOG.info("Add vertex for " + className + " with: " + className + " = " + value);
            oGraph.addVertex("class:" + className).setProperty(className, value);
        }
    }

    private Vertex getVertex(String className, Object value) {
        Iterable<Vertex> hits = oGraph.query().has(className, value).limit(1).vertices();
        if (hits != null && hits.iterator().hasNext()) {
            return hits.iterator().next();
        }
        return null;
    }

    private void createVertexType(String className) {
        if (oGraph.getVertexType(className) == null) {
            oGraph.createVertexType(className);
            oGraph.createKeyIndex(className, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        }
    }

    private void createEdgeType(String className) {
        if (oGraph.getEdgeType(className) == null) {
            oGraph.createEdgeType(className);
        }
    }

    public void committers(String author) {
        addVertex(V_COMMITTER, author);
    }

    public void revisions(String revision) {
        addVertex(V_REVISION, revision);
    }

    public void dates(DateTime date) {
        addVertex(V_DATE, date.toString());
    }

    public void files(String file) {
        addVertex(V_CLASS, file);
    }

    public void commits(Commit commit) {
        LOG.info("Processing Revision " + commit.getRevision());
        Vertex committer = getVertex(V_COMMITTER, commit.getCommitter());

        Vertex revision = getVertex(V_REVISION, commit.getRevision());
        revision.setProperty("message", commit.getMessage());

        Vertex date = getVertex(V_DATE, commit.getDate().toString());

        LOG.info(" -> Add Edge from " + commit.getCommitter() + " to " + commit.getRevision());
        oGraph.addEdge("class:" + E_COMMITTED, committer, revision, E_COMMITTED);

        LOG.info("  -> Add Edge from " + commit.getRevision() + " to " + commit.getDate().toString());
        oGraph.addEdge("class:" + E_COMMITTED_ON, revision, date, E_COMMITTED_ON);

        commit.getAffectedFiles().forEach(file -> {
            LOG.info(" -> Add Edge from " + commit.getRevision() + " to " + file);
            Vertex fileVertex = getVertex(V_CLASS, file);
            oGraph.addEdge("class:" + E_CONTAINS, revision, fileVertex, E_CONTAINS);
        });
    }

    OrientGraph getGraph() {
        return oGraph;
    }

}