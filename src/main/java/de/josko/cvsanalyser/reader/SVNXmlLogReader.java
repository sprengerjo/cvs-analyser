package de.josko.cvsanalyser.reader;

import de.josko.cvsanalyser.Commit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SVNXmlLogReader {
    protected final static Logger LOG = Logger.getLogger(SVNXmlLogReader.class.getSimpleName());

    private Document doc;
    private DocumentBuilder builder;

    private SVNXmlLogReader() {
        createBuilderFactory();
    }

    public SVNXmlLogReader(InputStream source) {
        this();

        try {
            doc = builder.parse(source);
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }

    private void createBuilderFactory() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getAuthors() {
        String xpath = "/log/logentry/author";
        return getStringValueSet(xpath);
    }


    public Set<DateTime> getDates() {
        String xpath = "/log/logentry/date";
        Set<String> dateStrings = getStringValueSet(xpath);
        Set<DateTime> dates = dateStrings.stream().map(dateString -> parseDate(dateString)).collect(Collectors.toSet());
        return dates;
    }

    public Set<String> getAffectedFiles() {
        String xpath = "/log/logentry/paths/path[@kind='file']";
        return getStringValueSet(xpath);
    }

    private Set<String> getStringValueSet(String xpath) {
        NodeList nodeList = selectNodes(xpath);

        Set<String> result = new HashSet<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add(nodeList.item(i).getTextContent());
        }

        return result;
    }

    private NodeList selectNodes(String authorXpathExpression) {
        return selectNodes(doc, authorXpathExpression);
    }

    private NodeList selectNodes(Node node, String xpathExpression) {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(xpathExpression);

            return (NodeList) expr.evaluate(node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Commit> getCommits() {
        ArrayList<Commit> commits = new ArrayList<Commit>();

        String xpath = "/log/logentry";
        NodeList commitNodes = selectNodes(xpath);
        for (int i = 0; i < commitNodes.getLength(); i++) {
            Node commitNode = commitNodes.item(i);

            Commit commit = new Commit();
            commit.setRevision(getRevision((Element) commitNode));
            commit.setAuthor(getAuthor(commitNode));
            commit.setDate(getDate(commitNode));
            commit.setAffectedFiles(getAffectedFilesFromCommit(commitNode));

            commits.add(commit);
        }

        return commits;
    }

    private DateTime getDate(Node commitNode) {
        String date = selectNodes(commitNode, "date").item(0).getTextContent();

        DateTime dateTime = parseDate(date);

        return dateTime;
    }

    private DateTime parseDate(String date) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        return dtf.parseDateTime(date);
    }

    private String getAuthor(Node commitNode) {
        return selectNodes(commitNode, "author").item(0).getTextContent();
    }

    private String getRevision(Element commitNode) {
        return commitNode.getAttribute("revision");
    }

    private Set<String> getAffectedFilesFromCommit(Node commitNode) {
        Set<String> affectedFiles = new HashSet<>();

        NodeList affectedFilesNodes = selectNodes(commitNode, "paths/path[@kind='file']");
        for (int j = 0; j < affectedFilesNodes.getLength(); j++) {
            Node affectedFileNode = affectedFilesNodes.item(j);
            affectedFiles.add(affectedFileNode.getTextContent());
        }

        return affectedFiles;
    }
}