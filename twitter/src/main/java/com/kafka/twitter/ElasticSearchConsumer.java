package com.kafka.twitter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ElasticSearchConsumer {

    public static final Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class);

    public static void main(String[] args) throws IOException {
        final RestHighLevelClient restHighLevelClient = createElasticSearchClient();

        final KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");
        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            final Integer recordsCount = records.count();
            logger.info("Received " + recordsCount + " records");

            if(recordsCount > 0) {
                final BulkRequest bulkRequest = new BulkRequest();

                for (final ConsumerRecord<String, String> consumerRecord : records) {
                    final String id;
                    try {
                        id = extractIdFromTweet(consumerRecord.value());
                        final IndexRequest indexRequest = new IndexRequest("twitter")
                                .source(consumerRecord.value(), XContentType.JSON)
                                .id(id);
                        bulkRequest.add(indexRequest);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

                final BulkResponse bulkItemResponses = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

                logger.info("Committing offsets");
                consumer.commitSync();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //restHighLevelClient.close();
    }

    public static RestHighLevelClient createElasticSearchClient() {
        final RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        );
        final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return restHighLevelClient;
    }

    public static KafkaConsumer<String, String> createConsumer(final String topic) {
        final Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "app");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(topic));
        return consumer;
    }

    public static String extractIdFromTweet(final String tweetJson) throws Exception {
        final JsonElement jsonElement = JsonParser.parseString(tweetJson)
                .getAsJsonObject()
                .get("id_str");
        if(jsonElement != null) return jsonElement.getAsString();
        else throw new Exception("No ID for tweet");
    }
}
