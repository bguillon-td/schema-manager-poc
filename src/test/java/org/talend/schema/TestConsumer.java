package org.talend.schema;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

import java.util.concurrent.CompletionStage;

public class TestConsumer {

    @Test
    public void testConsumer() throws Exception {

        ActorSystem system = ActorSystem.create("kafka-consumer");

        ActorMaterializer materializer = ActorMaterializer.create(system);

        final ConsumerSettings<String, String> consumerSettings =
        ConsumerSettings.create(system, new StringDeserializer(), new StringDeserializer())
                .withBootstrapServers("localhost:9092")
                .withGroupId("group1")
                .withClientId("Client")
                .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Source<ConsumerRecord<String, String>, Consumer.Control> source = Consumer.plainSource(consumerSettings, Subscriptions.topics("_schemas"));

        Sink<ConsumerRecord<String, String>, CompletionStage<Done>> sink = Sink.foreach(this::handleMessage);

        source.runWith(sink, materializer);

        system.awaitTermination();
    }

    private void handleMessage(ConsumerRecord<String, String> message){
        System.out.println("Got a message: " + message.key() + " / " + message.value());
    }


}
