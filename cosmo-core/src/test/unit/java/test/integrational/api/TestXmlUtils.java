package test.integrational.api;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.junit.Assert;
import org.springframework.util.xml.DomUtils;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestXmlUtils implements ExtendedDavConstants {
    private static final Log LOG = LogFactory.getLog(TestXmlUtils.class);
    protected  HttpResponse response;
    protected Document document;
    Map<String, List<Element>> parts = null;
    public Document getDocument() {
        return document;
    }

    TestXmlUtils(HttpResponse response) throws IOException, SAXException, ParserConfigurationException {
        this.response = response;
        Assert.assertEquals("text/xml", response.getEntity().getContentType().getElements()[0].getName());
        loadXml();
    }

    public int noOfResponses() {
        return getMultistatusParts().size();
    }

    private void loadXml() throws ParserConfigurationException, IOException, SAXException {
        String doc = IOUtils.toString(this.response.getEntity().getContent());

        this.document = DomUtil.parseDocument(IOUtils.toInputStream(doc));
        LOG.info(doc);
    }
    //Checks if D:multistatus is a root
    public void isMultistatusOK() {
        Assert.assertEquals(207, response.getStatusLine().getStatusCode()); //RFC Multi-Status code
        Element root = this.document.getDocumentElement();
        Assert.assertTrue(DomUtil.matches(root, "multistatus", NAMESPACE));
        Assert.assertNotNull(DomUtil.getChildElement(root, "response", NAMESPACE));
    }

    // Returns D:response


    public void containsHref(String expected) {
        Assert.assertTrue(getMultistatusParts().containsKey(expected));
    }

    public Map<String, List<Element>> getMultistatusParts() {
        if (parts == null) {
            parts = new HashMap<>();
            ElementIterator iterator = DomUtil.getChildren(this.document.getDocumentElement());
            while (iterator.hasNext()) {
                Element el = iterator.nextElement();
                Element href = DomUtil.getChildElement(el, "href", NAMESPACE);
                Assert.assertNotNull(href);

                List<Element> list = new ArrayList<Element>();
                ElementIterator children = DomUtil.getChildren(el);
                while (children.hasNext()) {
                    Element child = children.next();
                    if (DomUtil.matches(child, "href", NAMESPACE)) {
                        // it's a key, skip it
                        continue;
                    }
                    list.add(child);
                }

                parts.put(href.getTextContent(), list);
            }
        }

        return parts;
    }

    public Element findPropstatProp(String propName, Namespace namespace) {
        // Find property in D:propstat D:prop
        Element prop = DomUtil.getChildElement(
                getMultistatusParts().values().iterator().next().get(0), // This should be D:propstat
                "prop", NAMESPACE);
        Assert.assertNotNull(prop);
        Element propChild =  DomUtil.getChildElement(prop, propName, namespace);
        Assert.assertNotNull("No such property" + propName, propChild);
        return propChild;

    }

}
