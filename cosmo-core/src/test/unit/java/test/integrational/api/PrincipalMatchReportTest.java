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

import static org.apache.http.entity.ContentType.APPLICATION_XML;

public class PrincipalMatchReportTest extends AbstractMockIntegrationalTest {

    @Test
    public void testSelfUserPrincipalMatch() throws IOException, ParserConfigurationException, SAXException {
        HttpClient client = withUser(UserConstants.USER1_EMAIL, UserConstants.USER1_PASSWORD).build();
        RequestBuilder request = makeRequest("REPORT", "users");
        InputStream is = getInputStream("request/report/principal-match/self.xml");
        String payload = IOUtils.toString(is);
        request.setEntity(new StringEntity(payload, APPLICATION_XML));
        HttpResponse response = client.execute(request.build());
        TestXmlUtils utils = new TestXmlUtils(response);
        utils.isMultistatusOK();
        utils.containsHref(String.format("/cosmo/dav/users/%s", UserConstants.USER1_EMAIL));

        request = makeRequest("REPORT", "groups");
        request.setEntity(new StringEntity(payload, APPLICATION_XML));
        response = client.execute(request.build());
        utils = new TestXmlUtils(response);
        utils.isMultistatusOK();
        Assert.assertEquals(2, utils.noOfResponses()); // We have 2 groups for every registered user in our test authenticator configuration
        utils.containsHref(String.format("/cosmo/dav/groups/all"));
        utils.containsHref(String.format("/cosmo/dav/groups/all2"));
    }

    @Test
    public void testPrincipalPropertyOwnerPrincipalMatch() throws IOException, ParserConfigurationException, SAXException {
        HttpClient client = withUser(UserConstants.USER1_EMAIL, UserConstants.USER1_PASSWORD).build();
        String calendarPath =  "all/calendar";
        HttpUriRequest mkcalendar =  makeRequest("MKCALENDAR", calendarPath).build();;
        HttpResponse mkcalendarR = client.execute(mkcalendar);
        Assert.assertEquals(201, mkcalendarR.getStatusLine().getStatusCode());
        // User1 should own  all and all/calendar

        RequestBuilder report1 = makeRequest("REPORT", "all");
        // Load report
        InputStream is = getInputStream("request/report/principal-match/property-owner.xml");
        String payload = IOUtils.toString(is);
        report1.setEntity(new StringEntity(payload, APPLICATION_XML));
        HttpResponse responseReport1 = client.execute(report1.build());
        TestXmlUtils utils = new TestXmlUtils(responseReport1);
        utils.isMultistatusOK();
        Assert.assertEquals(2, utils.noOfResponses());
        utils.containsHref(String.format("/cosmo/dav/all/"));
        utils.containsHref(String.format("/cosmo/dav/all/calendar/"));

        //User2 should only own all
        HttpClient client2 = withUser(UserConstants.USER2_EMAIL, UserConstants.USER2_PASSWORD).build();
        HttpResponse responseReport2 = client2.execute(report1.build());
        utils = new TestXmlUtils(responseReport2);
        utils.isMultistatusOK();
        Assert.assertEquals(1, utils.noOfResponses());
        utils.containsHref(String.format("/cosmo/dav/all/"));

    }
}
