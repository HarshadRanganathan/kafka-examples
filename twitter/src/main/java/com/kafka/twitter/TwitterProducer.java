package com.kafka.twitter;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    public static final Logger logger = LoggerFactory.getLogger(TwitterProducer.class);

    final String consumerKey = "consumerKey";
    final String consumerSecret = "consumerSecret";
    final String token = "token";
    final String secret = "secret";

    public TwitterProducer() {}

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        final BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(1000);

        final Client hosebirdClient = createTwitterClient(msgQueue);
        hosebirdClient.connect();

        final KafkaProducer<String, String> producer = createKafkaProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping application");
            hosebirdClient.stop();
            producer.close();
            logger.info("Done");
        }));

        while (!hosebirdClient.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                hosebirdClient.stop();
            }
            if(msg!=null) {
                logger.info(msg);
                producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if(e != null) {
                            logger.error("Something bad happened!");
                        }
                    }
                });
            }
        }
        logger.info("End of Application");
    }

    public Client createTwitterClient(final BlockingQueue<String> msgQueue) {
        final Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        final StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        final List<String> terms = Lists.newArrayList("twitter", "api");
        hosebirdEndpoint.trackTerms(terms);

        final Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        final ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        final Client hosebirdClient = builder.build();
        return hosebirdClient;
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        final Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // settings for safe producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        // below are default properties when you enable idempotence
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // settings for high throughput producer
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));


        final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        return producer;
    }
}
