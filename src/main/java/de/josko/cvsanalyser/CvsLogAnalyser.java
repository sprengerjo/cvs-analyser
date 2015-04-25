package de.josko.cvsanalyser;

import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import de.josko.cvsanalyser.reader.LogReader;
import de.josko.cvsanalyser.reader.SVNXmlLogReader;

public class CvsLogAnalyser {
    public static final String COMMITTER = "Committer";
    public static final String REVISION = "Revision";
    public static final String DATE = "Date";
    public static final String CLASS = "Class";
    public static final String COMMITTED_TO = "committed";
    public static final String CONTAINS = "contains";
    private static final String DB_DIR = "localhost/test";
    private OrientGraphFactory factory;
    private OrientGraph oGraph;

    public static void main(String[] args) {
        new CvsLogAnalyser().run();
    }

    private void run() {
        factory = new OrientGraphFactory("remote:" + DB_DIR, "admin", "admin");
        oGraph = factory.getTx();
        oGraph.executeOutsideTx(iArgument -> {
            createVertexType(COMMITTER, "name");
            createVertexType(REVISION, "revision");
            createVertexType(DATE, "date");
            createVertexType(CLASS, "file");
            createEdgeType(COMMITTED_TO);
            createEdgeType(CONTAINS);


            LogReader reader = new SVNXmlLogReader(this.getClass().getResourceAsStream("log.xml"));

            reader.getAuthors().forEach(author -> addVertex(COMMITTER, "name", author));
            reader.getRevisions().forEach(revision -> addVertex(REVISION, "revision", revision));
            reader.getDates().forEach(date -> addVertex(DATE, "date", date.toString()));
            reader.getAffectedFiles().forEach(file -> addVertex(CLASS, "file", file));

            return null;
        });

        oGraph.shutdown();
        factory.close();
    }

    private void addVertex(String className, String key, String value) {
        String iKey = className + "." + key;
        for(Vertex v : oGraph.getVertices(iKey, value)) {
            return;
        }

        oGraph.addVertex("class:" + className).setProperty(key, value);
    }

    private void createVertexType(String className, String indexProperty) {
        if (oGraph.getVertexType(className) == null) {
            oGraph.createVertexType(className);
            oGraph.createKeyIndex(indexProperty, Vertex.class, new Parameter("class", className));
        }
    }

    private void createEdgeType(String className) {
        if (oGraph.getEdgeType(className) == null) {
            oGraph.createEdgeType(className);
        }
    }
}
