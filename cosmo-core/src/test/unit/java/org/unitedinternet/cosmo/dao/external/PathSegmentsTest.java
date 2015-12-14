package org.unitedinternet.cosmo.dao.external;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

/**
 * 
 * @author daniel grigore
 *
 */
public class PathSegmentsTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPassingNullPath() {
        new PathSegments(null);
    }

    @Test
    public void shouldGetEmptyStringsForEmptyPath() {
        PathSegments path = new PathSegments("");
        assertNotNull(path);
        assertTrue(path.getHomeUid().isEmpty());
        assertTrue(path.getCollectionUid().isEmpty());
        assertTrue(path.getEventUid().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreatePathWithOnlyHome() {
        String home = UUID.randomUUID().toString();
        PathSegments path = new PathSegments("/" + home);
        assertEquals(home, path.getHomeUid());
        assertTrue(path.getCollectionUid().isEmpty());
        assertTrue(path.getEventUid().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreatePathWithOnlyHomeAndCollection() {
        String home = UUID.randomUUID().toString();
        String collection = UUID.randomUUID().toString();
        PathSegments path = new PathSegments("/" + home + "/" + collection);
        assertEquals(home, path.getHomeUid());
        assertEquals(collection, path.getCollectionUid());
        assertTrue(path.getEventUid().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreatePathWithAllElements() {
        String home = UUID.randomUUID().toString();
        String collection = UUID.randomUUID().toString();
        String event = UUID.randomUUID().toString();
        PathSegments path = new PathSegments("/" + home + "/" + collection + "/" + event);
        assertEquals(home, path.getHomeUid());
        assertEquals(collection, path.getCollectionUid());
        assertEquals(event, path.getEventUid());
    }
}
