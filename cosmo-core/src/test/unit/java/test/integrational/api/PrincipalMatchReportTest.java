package test.integrational.api;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class PrincipalMatchReportTest extends AbstractMockIntegrationalTest {

    @Test
    public void testSelfUserPrincipalMatch() throws IOException, ParserConfigurationException, SAXException {
        HttpClient client = withUser(UserConstants.USER1_EMAIL, UserConstants.USER1_PASSWORD).build();
        RequestBuilder request = makeRequest("REPORT", "users");
        InputStream is = getInputStream("request/report/self-users.xml");
        String payload = IOUtils.toString(is);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
        HttpResponse response = client.execute(request.build());
        TestXmlUtils utils = new TestXmlUtils(response);
        utils.isMultistatusOK();
        utils.containsHref(String.format("/cosmo/dav/users/%s", UserConstants.USER1_EMAIL));

        request = makeRequest("REPORT", "groups");
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
        response = client.execute(request.build());
        utils = new TestXmlUtils(response);
        utils.isMultistatusOK();
        Assert.assertEquals(2, utils.noOfResponses()); // We have 2 groups for every registered user in our test authenticator configuration
        utils.containsHref(String.format("/cosmo/dav/groups/all"));
        utils.containsHref(String.format("/cosmo/dav/groups/all2"));
    }
}
