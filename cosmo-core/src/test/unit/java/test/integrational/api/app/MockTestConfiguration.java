package test.integrational.api.app;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.dao.mock.*;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;

@Profile("mocktest")
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class MockTestConfiguration {

    MockDaoStorage storage;



    MockTestConfiguration() {
        System.out.println("Initializing MockTestConfiguration...");
        storage = new MockDaoStorage();
    }
/*
//Only need that for programmatic security contexts
    @Bean
    @Primary
    public CosmoSecurityManager securityManager() {
        return new MockSecurityManager();
    }

 */
    @Bean
    @Primary
    public EntityFactory entityFactory() {
        return new MockEntityFactory();
    }

    @Bean
    @Primary
    public UserDao userDao() {
        return new MockUserDao(storage);
    }

    @Bean
    @Primary
    public ContentDao contentDao() {
        return new MockContentDao(storage);
    }

    @Bean
    @Primary
    public CalendarDao calendarDao() {
        return new MockCalendarDao(storage);
    }

}
