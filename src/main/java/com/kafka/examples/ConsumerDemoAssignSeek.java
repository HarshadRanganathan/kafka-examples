package com.kafka.examples;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemoAssignSeek {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class);

    public static void main(String[] args) {
        final Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        final TopicPartition partitionToReadFrom = new TopicPartition("test-topic", 1);
        consumer.assign(Collections.singleton(partitionToReadFrom));
        consumer.seek(partitionToReadFrom, 1);

        int numberOfMessagesToRead = 5;
        int numberOfMessagesReadSoFar = 0;
        boolean keepReading = true;

        while (keepReading) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for(final ConsumerRecord<String, String> consumerRecord: records) {
                numberOfMessagesReadSoFar += 1;
                logger.info("Key: " + consumerRecord.key() + " Value: " + consumerRecord.value());
                logger.info("Partition: " + consumerRecord.partition() + " Offset: " + consumerRecord.offset());
                if(numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
                    keepReading = false;
                    break;
                }
            }
        }
    }
}
