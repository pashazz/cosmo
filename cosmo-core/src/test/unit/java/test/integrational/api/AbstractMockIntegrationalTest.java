package test.integrational.api;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.UserService;

import java.util.Map;

@ActiveProfiles(value = { "test", "mocktest" })
public abstract class AbstractMockIntegrationalTest extends AbstractIntegrationalTest {

}
