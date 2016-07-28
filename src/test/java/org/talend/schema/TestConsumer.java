package org.talend.schema;

import org.apache.avro.Schema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestConsumer {


    @Test
    public void testConsumer() throws Exception {
        KafkaConsumer<String, String> consumer = createConsumer("localhost:9092", "schemamanager", 100);
        String topic = "_schemas";
        //consumer.subscribe(Collections.singleton(topic));
        assignPartitions(consumer, topic);

        while(true){
            ConsumerRecords<String, String> messages = consumer.poll(100);
            messages.forEach(message -> {
                if(message.value() != null){
                    System.out.println("Key : " + message.key() + ", value : " + message.value());

                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Map wrapperMessage = objectMapper.readValue(message.value(), Map.class);
                        Schema schema = new Schema.Parser().parse((String) wrapperMessage.get("schema"));
                        System.out.println("Schema: " + schema.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });
        }


    }

    private static void assignPartitions(KafkaConsumer<String, String> consumer, String topic) {
        List<TopicPartition> topicPartitions = consumer.partitionsFor(topic).stream().map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition())).collect(Collectors.toList());
        consumer.assign(topicPartitions);
        consumer.seekToBeginning(topicPartitions);
    }

    private KafkaConsumer<String, String> createConsumer(String broker, String group, int maxPollRecords) throws Exception {
        Map<String, Object> conf = new HashMap<>();
        conf.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        conf.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        conf.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        conf.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        conf.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        conf.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        return new KafkaConsumer<>(conf);

    }


}
