package com.kafka.examples;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

    public static void main(String[] args) {
        new ConsumerDemo().run();
    }

    private void run() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Runnable consumerRunnable = new ConsumerRunnable(countDownLatch);
        final Thread thread = new Thread(consumerRunnable);

        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook");
            ((ConsumerRunnable) consumerRunnable).shutdown();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.info("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

        private CountDownLatch countDownLatch;
        private KafkaConsumer<String, String> consumer;

        public ConsumerRunnable(final CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;

            final Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "app");
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Collections.singleton("test-topic"));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for(final ConsumerRecord<String, String> consumerRecord: records) {
                        logger.info("Key: " + consumerRecord.key() + " Value: " + consumerRecord.value());
                        logger.info("Partition: " + consumerRecord.partition() + " Offset: " + consumerRecord.offset());
                    }
                }
            } catch (WakeupException e) {
                logger.info("Received shutdown signal");
            } finally {
                consumer.close();
                countDownLatch.countDown();
            }
        }

        public void shutdown() {
            consumer.wakeup();
        }
    }
}
