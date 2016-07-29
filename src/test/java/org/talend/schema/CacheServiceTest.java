package org.talend.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testPut() {
        SchemaSummary schemaSummary1 = new SchemaSummary();
        schemaSummary1.setNamespace("org.talend");
        schemaSummary1.setName("record1");
        schemaSummary1.setDescription("Short description");
        schemaSummary1.setVersion(0);

        SchemaSummary schemaSummary2 = new SchemaSummary();
        schemaSummary2.setNamespace("org.talend");
        schemaSummary2.setName("record1");
        schemaSummary2.setDescription("Short description");
        schemaSummary2.setVersion(1);

        cacheService.put("org.talend", "record1", schemaSummary1);

        // no previous element in cache, the schema summary is stored
        assertEquals(1, cacheService.getSchemaTable().size());
        assertEquals(schemaSummary1, cacheService.getSchemaTable().get("org.talend", "record1"));

        cacheService.put("org.talend", "record1", schemaSummary2);

        // existing element in cache with a previous version, the cache is updated
        assertEquals(1, cacheService.getSchemaTable().size());
        assertEquals(schemaSummary2, cacheService.getSchemaTable().get("org.talend", "record1"));

        cacheService.put("org.talend", "record1", schemaSummary1);

        // existing element in cache with a newer version, the cache is not updated
        assertEquals(1, cacheService.getSchemaTable().size());
        assertEquals(schemaSummary2, cacheService.getSchemaTable().get("org.talend", "record1"));
    }

    @Test
    public void methodPutHasSynchronizedModifier() throws Exception {
        Method m = CacheService.class.getMethod("put", String.class, String.class, SchemaSummary.class);
        assertTrue(Modifier.isSynchronized(m.getModifiers()));
    }
}