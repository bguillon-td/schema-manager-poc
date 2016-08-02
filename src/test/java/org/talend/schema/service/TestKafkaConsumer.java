package org.talend.schema.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
public class TestKafkaConsumer {

    @InjectMocks
    @Autowired
    private KafkaConsumer kafkaConsumer = new KafkaConsumer();

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
    public void testHandleMessage_correct_message_expect_cache_is_updated() throws Exception {
        // schema with a record type, and optional parameters: name, namespace, doc
        ConsumerRecord<String, String> message = new ConsumerRecord<>("testTopic", 0, 0,
                "{\"subject\":\"schema5\",\"version\":1,\"magic\":0,\"keytype\":\"SCHEMA\"}",
                "{\"subject\":\"schema5\",\"version\":1,\"id\":1,\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"doc\\\":\\\"a short description\\\",\\\"name\\\":\\\"record6\\\",\\\"namespace\\\":\\\"org.talend\\\",\\\"fields\\\":[{\\\"name\\\":\\\"value\\\",\\\"type\\\":\\\"long\\\"}]}\"}");

        kafkaConsumer.handleMessage(message);

        verify(cacheService).putSchemaSummary(eventCaptor.capture());
        SchemaSummary schemaSummary = eventCaptor.getValue();

        // schema summary is complete
        assertNotNull(schemaSummary);
        assertEquals("org.talend", schemaSummary.getNamespace());
        assertEquals("record6", schemaSummary.getName());
        assertEquals("a short description", schemaSummary.getDescription());
        assertEquals((Integer) 1, schemaSummary.getVersion());
    }

    @Test
    public void testHandleMessage_schema_without_name_expect_cache_is_not_updated() throws Exception {
        // schema without name, namespace and doc fields
        ConsumerRecord<String, String> message = new ConsumerRecord<>("testTopic", 0, 0,
                "{\"subject\":\"schema5\",\"version\":1,\"magic\":0,\"keytype\":\"SCHEMA\"}",
                "{\"subject\":\"schema5\",\"version\":1,\"id\":1,\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"record1\\\",\\\"fields\\\":[{\\\"name\\\":\\\"value\\\",\\\"type\\\":\\\"long\\\"}]}\"}");
        kafkaConsumer.handleMessage(message);

        // cache is not updated with the incorrect schema registry
        verify(cacheService, never()).putSchemaSummary(any(SchemaSummary.class));
    }

    @Test
    public void testHandleMessage_noop_message_expect_cache_is_not_updated() throws Exception {
        // NOOP message, without value
        ConsumerRecord<String, String> message = new ConsumerRecord<>("testTopic", 0, 0, "{\"magic\":0,\"keytype\":\"NOOP\"}",
                null);
        kafkaConsumer.handleMessage(message);

        // cache is not updated with the incorrect schema registry
        verify(cacheService, never()).putSchemaSummary(any(SchemaSummary.class));
    }

    @Test
    public void testHandleMessage_primitive_type_schema_expect_cache_is_not_updated() throws Exception {
        // schema with a primitive type
        ConsumerRecord<String, String> message = new ConsumerRecord<>("testTopic", 0, 0,
                "{\"subject\":\"schema5\",\"version\":1,\"magic\":0,\"keytype\":\"SCHEMA\"}",
                "{\"subject\":\"schema5\",\"version\":1,\"id\":1,\"schema\":\"{\\\"type\\\":\\\"string\\\"}\"}");
        kafkaConsumer.handleMessage(message);

        // cache is not updated with the incorrect schema registry
        verify(cacheService, never()).putSchemaSummary(any(SchemaSummary.class));
    }

}