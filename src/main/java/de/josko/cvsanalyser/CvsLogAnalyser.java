package de.josko.cvsanalyser;

import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import de.josko.cvsanalyser.reader.LogReader;
import de.josko.cvsanalyser.reader.SVNXmlLogReader;

import java.util.logging.Logger;

public class CvsLogAnalyser {
    public static final String V_COMMITTER = "Committer";
    public static final String V_REVISION = "Revision";
    public static final String V_DATE = "Date";
    public static final String V_CLASS = "Class";
    public static final String E_COMMITTED = "committed";
    public static final String E_CONTAINS = "contains";
    public static final String E_COMMITTED_ON = "committedOn";
    private final static Logger LOG = Logger.getLogger(CvsLogAnalyser.class.getSimpleName());
    private static final String DB_DIR = "localhost/test";

    private OrientGraphFactory factory;
    private OrientGraph oGraph;

    public static void main(String[] args) {
        new CvsLogAnalyser().run();
    }

    private void run() {
        factory = new OrientGraphFactory("remote:" + DB_DIR, "root", "root");
        oGraph = factory.getTx();
        oGraph.executeOutsideTx(iArgument -> {
            createVertexType(V_COMMITTER, "name");
            createVertexType(V_REVISION, "revision");
            createVertexType(V_DATE, "date");
            createVertexType(V_CLASS, "file");
            createEdgeType(E_COMMITTED);
            createEdgeType(E_CONTAINS);
            createEdgeType(E_COMMITTED_ON);


            LogReader reader = new SVNXmlLogReader(this.getClass().getResourceAsStream("log.xml"));

            reader.getAuthors().forEach(author -> {
                addVertex(V_COMMITTER, "name", author);
            });
            reader.getRevisions().forEach(revision -> {
                addVertex(V_REVISION, "revision", revision);
            });
            reader.getDates().forEach(date -> {
                addVertex(V_DATE, "date", date.toString());
            });
            reader.getAffectedFiles().forEach(file -> {
                addVertex(V_CLASS, "file", file);
            });

            reader.getCommits().forEach(commit -> {
                LOG.info("Processing Revision " + commit.getRevision());
                Vertex committer = getVertex(V_COMMITTER, "name", commit.getCommitter());
                Vertex revision = getVertex(V_REVISION, "revision", commit.getRevision());
                Vertex date = getVertex(V_DATE, "date", commit.getDate().toString());

                LOG.info(" -> Add Edge from " + commit.getCommitter() + " to " + commit.getRevision());
                oGraph.addEdge("class:" + E_COMMITTED, committer, revision, E_COMMITTED);

                LOG.info("  -> Add Edge from " + commit.getRevision() + " to " + commit.getDate().toString());
                oGraph.addEdge("class:" + E_COMMITTED_ON, revision, date, E_COMMITTED_ON);

                commit.getAffectedFiles().forEach(file -> {
                    LOG.info(" -> Add Edge from " + commit.getRevision() + " to " + file);
                    Vertex fileVertex = getVertex(V_CLASS, "file", file);
                    oGraph.addEdge("class:" + E_CONTAINS, revision, fileVertex, E_CONTAINS);
                });
            });
            return null;
        });

        oGraph.shutdown();
        factory.close();
    }

    private void addVertex(String className, String key, Object value) {
        Vertex vertex = getVertex(className, key, value);
        if (vertex == null) {
            LOG.info("Add vertex for " + className + " with: " + key + " = " + value);
            oGraph.addVertex("class:" + className).setProperty(key, value);
        }
    }

    private Vertex getVertex(String className, String key, Object value) {
        Iterable<Vertex> hits = oGraph.query().has(key, value).limit(1).vertices();
        if (hits != null && hits.iterator().hasNext()) {
            return hits.iterator().next();
        }
        return null;
    }

    private void createVertexType(String className, String indexProperty) {
        if (oGraph.getVertexType(className) == null) {
            oGraph.createVertexType(className);
            oGraph.createKeyIndex(indexProperty, Vertex.class, new Parameter("type", "UNIQUE"), new Parameter("class", className));
        }
    }

    private void createEdgeType(String className) {
        if (oGraph.getEdgeType(className) == null) {
            oGraph.createEdgeType(className);
        }
    }
}
