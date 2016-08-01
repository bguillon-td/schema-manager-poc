package org.talend.schema.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.schema.Application;
import org.talend.schema.model.SchemaSummary;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class KafkaConsumerTest {

    @InjectMocks
    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Mock
    private CacheServiceImpl cacheService;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<SchemaSummary> eventCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleMessage() throws Exception {
        ConsumerRecord<String, String> message = new ConsumerRecord<>("testTopic", 0, 0,
                "{\"subject\":\"schema5\",\"version\":1,\"magic\":0,\"keytype\":\"SCHEMA\"}",
                "{\"subject\":\"schema5\",\"version\":1,\"id\":1,\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"doc\\\":\\\"a short description\\\",\\\"name\\\":\\\"record6\\\",\\\"namespace\\\":\\\"org.talend\\\",\\\"fields\\\":[{\\\"name\\\":\\\"value\\\",\\\"type\\\":\\\"long\\\"}]}\"}");

        kafkaConsumer.handleMessage(message);

        Mockito.verify(cacheService).putSchemaSummary(eventCaptor.capture());
        SchemaSummary schemaSummary = eventCaptor.getValue();

        assertNotNull(schemaSummary);
        assertEquals("org.talend", schemaSummary.getNamespace());
        assertEquals("record6", schemaSummary.getName());
        assertEquals("a short description", schemaSummary.getDescription());
        assertEquals((Integer) 1, schemaSummary.getVersion());
    }

}