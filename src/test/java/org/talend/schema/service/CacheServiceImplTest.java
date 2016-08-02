package org.talend.schema.service;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.schema.Application;
import org.talend.schema.model.SchemaSummary;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class CacheServiceImplTest {

    @Autowired
    private CacheServiceImpl cacheService;

    @Before
    public void setup() {
        cacheService.getSchemaTable().clear();
    }

    @Test
    public void testPutSchemaSummary() {
        SchemaSummary schemaSummary1 = new SchemaSummary();
        schemaSummary1.setNamespace("org.talend");
        schemaSummary1.setName("record1");
        // schemaSummary1.setDescription("Short description");
        schemaSummary1.setVersion(0);

        SchemaSummary schemaSummary2 = new SchemaSummary();
        schemaSummary2.setNamespace("org.talend");
        schemaSummary2.setName("record1");
        schemaSummary2.setDescription("Short description");
        schemaSummary2.setVersion(1);

        cacheService.putSchemaSummary(schemaSummary1);

        // no previous element in cache, the schema summary is stored
        assertEquals(1, cacheService.getSchemaTable().size());
        assertEquals(schemaSummary1, cacheService.getSchemaTable().get("org.talend", "record1"));

        cacheService.putSchemaSummary(schemaSummary2);

        // existing element in cache with a previous version, the cache is updated
        assertEquals(1, cacheService.getSchemaTable().size());
        assertEquals(schemaSummary2, cacheService.getSchemaTable().get("org.talend", "record1"));

        cacheService.putSchemaSummary(schemaSummary1);

        // existing element in cache with a newer version, the cache is not updated
        assertEquals(1, cacheService.getSchemaTable().size());
        assertEquals(schemaSummary2, cacheService.getSchemaTable().get("org.talend", "record1"));
    }

    @Test
    public void methodPutHasSynchronizedModifier() throws Exception {
        Method m = CacheServiceImpl.class.getMethod("putSchemaSummary", SchemaSummary.class);
        assertTrue(Modifier.isSynchronized(m.getModifiers()));
    }

    @Test
    public void testGetSchemaSummaries() {
        Collection<SchemaSummary> schemaSummaryList = cacheService.getSchemaSummaries("org.talend");
        assertEquals(0, schemaSummaryList.size());

        SchemaSummary schemaSummary1 = new SchemaSummary();
        schemaSummary1.setNamespace("org.talend");
        schemaSummary1.setName("r_record");
        schemaSummary1.setDescription("Short description");
        schemaSummary1.setVersion(0);

        SchemaSummary schemaSummary2 = new SchemaSummary();
        schemaSummary2.setNamespace("org.talend");
        schemaSummary2.setName("s_record");
        schemaSummary2.setDescription("Short description");
        schemaSummary2.setVersion(0);

        SchemaSummary schemaSummary3 = new SchemaSummary();
        schemaSummary3.setNamespace("org.talend");
        schemaSummary3.setName("a_record");
        schemaSummary3.setDescription("Short description");
        schemaSummary3.setVersion(0);

        // put 3 schema summaries into cache, on the same namespace and with different names
        cacheService.putSchemaSummary(schemaSummary1);
        cacheService.putSchemaSummary(schemaSummary2);
        cacheService.putSchemaSummary(schemaSummary3);

        schemaSummaryList = cacheService.getSchemaSummaries("org.talend");

        // get the 3 schema summaries, sorted by name
        assertEquals(schemaSummaryList.size(), 3);
        Iterator<SchemaSummary> iterator = schemaSummaryList.iterator();
        assertEquals(schemaSummary3, iterator.next());
        assertEquals(schemaSummary1, iterator.next());
        assertEquals(schemaSummary2, iterator.next());
    }

    @Test
    public void testGetSchemaSummary() {
        Optional<SchemaSummary> schemaSummaryResult = cacheService.getSchemaSummary("org.talend", "r_record");
        assertFalse(schemaSummaryResult.isPresent());

        SchemaSummary schemaSummary1 = new SchemaSummary();
        schemaSummary1.setNamespace("org.talend");
        schemaSummary1.setName("r_record");
        schemaSummary1.setDescription("Short description");
        schemaSummary1.setVersion(0);

        cacheService.putSchemaSummary(schemaSummary1);

        schemaSummaryResult = cacheService.getSchemaSummary("org.talend", "r_record");
        assertTrue(schemaSummaryResult.isPresent());
        assertEquals(schemaSummary1, schemaSummaryResult.get());
    }
}