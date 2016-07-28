package org.talend.schema;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Component
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    private ActorSystem system;

    private ActorMaterializer materializer;

    private Sink<ConsumerRecord<String, String>, CompletionStage<Done>> sink;

    private Source<ConsumerRecord<String, String>, Consumer.Control> source;

    @PostConstruct
    public void setup() throws Exception {
        LOG.info("Starting consumer");
        system = ActorSystem.create("kafka-consumer");
        materializer = ActorMaterializer.create(system);
        final ConsumerSettings<String, String> consumerSettings =
                ConsumerSettings.create(system, new StringDeserializer(), new StringDeserializer())
                        .withBootstrapServers("localhost:9092")
                        .withGroupId("group1")
                        .withClientId("Client")
                        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.source = Consumer.plainSource(consumerSettings, Subscriptions.topics("_schemas"));

        this.sink = Sink.foreach(this::handleMessage);

        source.runWith(sink, materializer);

        LOG.info("End of consumer start");
    }

    private void handleMessage(ConsumerRecord<String, String> message){
        LOG.info("Got a message: " + message.key() + " / " + message.value());
        if(message.value() != null){
            LOG.info("Key : " + message.key() + ", value : " + message.value());
            try {
                Map wrapperMessage = objectMapper.readValue(message.value(), Map.class);
                Schema schema = new Schema.Parser().parse((String) wrapperMessage.get("schema"));
                LOG.info("Schema: " + schema.toString());
            } catch (Exception e) {
                LOG.error("Error in consumer", e);
            }
        }
    }

    @PreDestroy
    public void shutdown() throws Exception {
        LOG.info("Starting consumer shutdown");
        if(system != null){
            system.shutdown();
        }
        LOG.info("Consumer shutdown completed");
    }

}
