package org.talend.schema.service;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.avro.Schema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.schema.model.SchemaSummary;

import com.fasterxml.jackson.databind.ObjectMapper;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

@Component
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheServiceImpl cacheService;

    private ActorSystem system;

    private ActorMaterializer materializer;

    private Sink<ConsumerRecord<String, String>, CompletionStage<Done>> sink;

    private Source<ConsumerRecord<String, String>, Consumer.Control> source;

    @Value("${kafka.broker}")
    private String kafkaBrokerUrl;

    @Value("${kafka.group}")
    private String kafkaGroup;

    @Value("${kafka.clientId}")
    private String kafkaClientId;

    @Value("${kafka.resetConfig}")
    private String kafkaResetConfig;

    @Value("${kafka.enableAutoCommit}")
    private String kafkaEnableAutoCommit;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    @PostConstruct
    public void setup() throws Exception {
        LOG.info("Starting consumer");
        system = ActorSystem.create("kafka-consumer");
        materializer = ActorMaterializer.create(system);
        final ConsumerSettings<String, String> consumerSettings = ConsumerSettings
                .create(system, new StringDeserializer(), new StringDeserializer()).withBootstrapServers(kafkaBrokerUrl)
                .withGroupId(kafkaGroup).withClientId(kafkaClientId)
                .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaEnableAutoCommit)
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaResetConfig);

        this.source = Consumer.plainSource(consumerSettings, Subscriptions.topics(kafkaTopic));

        this.sink = Sink.foreach(this::handleMessage);

        source.runWith(sink, materializer);

        LOG.info("End of consumer start");
    }

    protected void handleMessage(ConsumerRecord<String, String> message) {
        try {
            LOG.info("Got a message: " + message.key() + " / " + message.value());
            Map wrapperKey = objectMapper.readValue(message.key(), Map.class);
            if ("SCHEMA".equals(wrapperKey.get("keytype")) && message.value() != null) {
                Map wrapperMessage = objectMapper.readValue(message.value(), Map.class);
                Schema schema = new Schema.Parser().parse((String) wrapperMessage.get("schema"));

                SchemaSummary schemaSummary = new SchemaSummary();
                schemaSummary.setNamespace(schema.getNamespace());
                schemaSummary.setName(schema.getName());
                schemaSummary.setDescription(schema.getDoc());
                schemaSummary.setVersion((Integer) wrapperMessage.get("version"));
                this.cacheService.putSchemaSummary(schemaSummary);
            }
        } catch (Exception exception) {
            LOG.warn("Following message was not stored in the schema summary cache : " + message.key() + " / " + message.value(),
                    exception);
        }
    }

    @PreDestroy
    public void shutdown() throws Exception {
        LOG.info("Starting consumer shutdown");
        if (system != null) {
            system.shutdown();
        }
        LOG.info("Consumer shutdown completed");
    }

}
