package de.josko.cvsanalyser.db;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.*;
import de.josko.cvsanalyser.reader.Commit;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

public class OrientDbWriter {

    public static final String V_COMMITTER = "Committer";
    public static final String V_REVISION = "Revision";
    public static final String V_DATE = "Date";
    public static final String V_CLASS = "Class";
    static final String E_COMMITTED = "committed";
    static final String E_CONTAINS = "contains";
    static final String E_COMMITTED_ON = "committedOn";
    private static final String DB_DIR = "remote:localhost/db";
    private final static Logger LOG = Logger.getLogger(OrientDbWriter.class);
    List<String> vertices = Arrays.asList(
            V_COMMITTER,
            V_REVISION,
            V_DATE,
            V_CLASS
    );
    List<String> edges = Arrays.asList(
            E_COMMITTED,
            E_CONTAINS,
            E_COMMITTED_ON
    );
    private OrientGraphNoTx noTxGraph;
    private OrientGraphFactory factory;
    private OrientGraph txGraph;

    OrientDbWriter(OrientGraphFactory factory) {
        this.factory = factory;
        txGraph = factory.getTx();
        noTxGraph = factory.getNoTx();
        setUpVertexesAndEdges();
    }

    public OrientDbWriter() {
        openConnection();
        setUpVertexesAndEdges();
    }

    private void setUpVertexesAndEdges() {
        vertices.stream().forEach(v -> createVertexType(v));
        edges.stream().forEach(e -> createEdgeType(e));
    }

    private void openConnection() {
        factory = new OrientGraphFactory(DB_DIR, "root", "root");
        txGraph = factory.getTx();
        noTxGraph = factory.getNoTx();
    }

    public void shutDown() {
        noTxGraph.shutdown();
        txGraph.shutdown();
        factory.close();
    }

    public Vertex createVertex(String className, Object value) {
        LOG.info("Add vertex for " + className + " with: " + className + " = " + value);
        Vertex vertex = obtainVertex(className, value);
        if (vertex == null) {
            vertex = txGraph.addVertex("class:" + className);
            vertex.setProperty(className, value);
        }
        return vertex;
    }

    private Vertex obtainVertex(String className, Object value) {
        Iterable<Vertex> hits = txGraph.query().has(className, value).limit(1).vertices();
        if (hits != null && hits.iterator().hasNext()) {
            return hits.iterator().next();
        }
        return null;
    }

    private void createVertexType(String className) {
        if (noTxGraph.getVertexType(className) == null) {
            OrientVertexType vertexType = noTxGraph.createVertexType(className);
            vertexType.createProperty("asd", OType.LINK);
            noTxGraph.createKeyIndex(className, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        }
    }

    private void createEdgeType(String className) {
        if (noTxGraph.getEdgeType(className) == null) {
            OrientEdgeType edgeType = noTxGraph.createEdgeType(className);
        }
    }

    public Vertex createCommitterVertex(String author) {
        return createVertex(V_COMMITTER, author);
    }

    public Vertex createRevisionVertex(String revision) {
        return createVertex(V_REVISION, revision);
    }

    public Vertex createDateVertex(DateTime date) {
        return createVertex(V_DATE, date.toString());
    }

    public Vertex createFileVertex(String file) {
        return createVertex(V_CLASS, file);
    }

    public void processCommits(Commit commit) {
        // Skip revison if already in database
        if (obtainVertex(V_REVISION, commit.getRevision()) == null) {
            LOG.info("Processing Revision " + commit.getRevision());

            Vertex committer = createCommitterVertex(commit.getCommitter());

            Vertex revision = createRevisionVertex(commit.getRevision());
            revision.setProperty("message", commit.getMessage());

            Vertex date = createDateVertex(commit.getDate());

            LOG.info(" -> Add Edge from " + commit.getCommitter() + " to " + commit.getRevision());
            txGraph.addEdge("class:" + E_COMMITTED, committer, revision, E_COMMITTED);

            LOG.info(" -> Add Edge from " + commit.getRevision() + " to " + commit.getDate().toString());
            txGraph.addEdge("class:" + E_COMMITTED_ON, revision, date, E_COMMITTED_ON);

            commit.getAffectedFiles().forEach(file -> {
                LOG.info(" -> Add Edge from " + commit.getRevision() + " to " + file);
                Vertex fileVertex = createFileVertex(file);
                txGraph.addEdge("class:" + E_CONTAINS, revision, fileVertex, E_CONTAINS);
            });

            txGraph.commit();
        } else {
            LOG.info("Skipping Revision " + commit.getRevision() + ". Already exists.");
        }
    }

    OrientGraph getGraph() {
        return txGraph;
    }
}