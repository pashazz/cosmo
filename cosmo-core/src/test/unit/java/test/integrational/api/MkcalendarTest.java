package test.integrational.api;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.XmlEntity;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.hibernate.mapping.Any;
import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.acl.AnyAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.DavPrivilegeSet;
import org.unitedinternet.cosmo.dav.acl.property.Acl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.jackrabbit.webdav.DavConstants.NAMESPACE;
import static org.unitedinternet.cosmo.dav.acl.AclConstants.ELEMENT_ACL_ACE;


public class MkcalendarTest extends AbstractMockIntegrationalTest{
    

    protected RequestBuilder makeMkcalendar(String path) {
        return makeRequest("MKCALENDAR", path);
    }

    public InputStream getInputStream(String name)  {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return in;
    }
    @Test
    public void testMkcalendar() throws IOException {
        HttpClientBuilder builder = withUser(UserConstants.USER1_EMAIL, UserConstants.USER1_PASSWORD);
        HttpClient client = builder.build();
        HttpUriRequest request = makeMkcalendar(UserConstants.USER1_EMAIL + "/calendar").build();
        HttpResponse response = client.execute(request);
        //Should return response
        String resp = IOUtils.toString(response.getEntity().getContent());
        System.err.println(resp);
        // should return 201 OK
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

    }

    /**
     * Executes a propfind for calendar collection examining current-user-privilege-set
     * for an owner, then for another user, then for another user after ACL on current collection
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test
    public void testPropfindCUPSForCalendarCollection() throws IOException, ParserConfigurationException, SAXException {
        //Make user1
        HttpClient client = withUser(UserConstants.USER1_EMAIL, UserConstants.USER1_PASSWORD).build();
        HttpUriRequest request =  makeMkcalendar(UserConstants.USER1_EMAIL + "/calendar").build();;
        HttpResponse response = client.execute(request);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        //Make propfind for  user1
        RequestBuilder builder = makeRequest("PROPFIND", UserConstants.USER1_EMAIL + "/calendar");
        String payload = IOUtils.toString(getInputStream("request/cups/propfind.xml"));

        HttpEntity propfindPayload = new StringEntity(payload, ContentType.APPLICATION_XML);
        builder.setEntity(propfindPayload);
        request = builder.build();
        client = withUser(UserConstants.USER1_EMAIL, UserConstants.USER1_PASSWORD).build();
        response = client.execute(request);
        TestXmlUtils utils = new TestXmlUtils(response);

        // Read response
        utils.isMultistatusOK();
        utils.multistatusHrefEquals(String.format("/cosmo/dav/%s/calendar/", UserConstants.USER1_EMAIL));

        // CPSU should contain everything for us as we're the owners

        Element CUPS = utils.findPropstatProp("current-user-privilege-set", NAMESPACE);
        DavPrivilegeSet dps = DavPrivilegeSet.fromXmlAcl(CUPS);
        Assert.assertTrue(dps.containsRecursive(DavPrivilege.READ));
        Assert.assertTrue(dps.containsRecursive(DavPrivilege.WRITE));
        Assert.assertTrue(dps.containsRecursive(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET));
        Assert.assertTrue(dps.containsRecursive(DavPrivilege.READ_FREE_BUSY));

        //Make user2
        HttpClient client2 = withUser(UserConstants.USER2_EMAIL, UserConstants.USER2_PASSWORD).build();
        HttpUriRequest request2 =  makeMkcalendar(UserConstants.USER2_EMAIL + "/calendar").build();;
        HttpResponse response2 = client2.execute(request2);
        Assert.assertEquals(201, response2.getStatusLine().getStatusCode());

        //Execute ACL on user1
        RequestBuilder aclRequest = makeRequest("ACL", UserConstants.USER1_EMAIL + "/calendar");
        String aclPayload = IOUtils.toString(getInputStream("request/cups/acl.xml"));
        String aclPayloadConcrete = aclPayload.replace("HREF", String.format("http://localhost:%d/cosmo/dav/users/%s/", port, UserConstants.USER2_EMAIL));
        StringEntity entity = new StringEntity(aclPayloadConcrete, ContentType.APPLICATION_XML);
        aclRequest.setEntity(entity);
        response = client.execute(aclRequest.build());
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());


        //Execute propfind with same payload using user2 (client2)
        builder = makeRequest("PROPFIND", UserConstants.USER1_EMAIL + "/calendar");
        propfindPayload = new StringEntity(payload, ContentType.APPLICATION_XML);
        builder.setEntity(propfindPayload);
        request = builder.build();
        response = client2.execute(request);
        utils = new TestXmlUtils(response);

         // Read response
        utils.isMultistatusOK();
        utils.multistatusHrefEquals(String.format("/cosmo/dav/%s/calendar/", UserConstants.USER1_EMAIL));

        // CPSU should contain ONLY DAV:read and DAV:r-c-u-p-s
        CUPS = utils.findPropstatProp("current-user-privilege-set", NAMESPACE);
        dps = DavPrivilegeSet.fromXmlAcl(CUPS);
        Assert.assertTrue(dps.containsRecursive(DavPrivilege.READ));
        Assert.assertFalse(dps.containsRecursive(DavPrivilege.WRITE));
        Assert.assertTrue(dps.containsRecursive(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET)); // Protected privilege
        Assert.assertFalse(dps.containsRecursive(DavPrivilege.READ_FREE_BUSY));


    }


   protected Set<AnyAce> parseAceSet(Element root) throws CosmoDavException {
        Set<AnyAce> aces = new HashSet<>();
        ElementIterator iter = DomUtil.getChildren(root);
        while (iter.hasNext()) {
            Element aceElement = iter.next();
            Assert.assertTrue("acl element contains non-ace element", DomUtil.matches(aceElement, ELEMENT_ACL_ACE, NAMESPACE));
            aces.add(AnyAce.fromXml(aceElement));
        }
        return aces;
    }


}


