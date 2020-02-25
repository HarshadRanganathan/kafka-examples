package com.kafka.examples;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    public static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class);

    public static void main(String[] args) {
        final Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        final ProducerRecord<String, String> record = new ProducerRecord<>("test-topic", "1", "Hello World");

        producer.send(record, new Callback() {
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e == null){
                    logger.info("Received new metadata: \n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp());
                } else {
                    logger.error("Error while producing: " + e);
                }
            }
        });
        producer.flush();
        producer.close();
    }
}
