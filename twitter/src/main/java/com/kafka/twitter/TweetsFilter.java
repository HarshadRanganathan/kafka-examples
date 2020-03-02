package com.kafka.twitter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class TweetsFilter {

    public static void main(String[] args) {
        final Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final KStream<String, String> streamTopic = streamsBuilder.stream("twitter_tweets");
        final KStream<String, String> filteredStream = streamTopic.filter(
                (k, jsonTweet) -> extractFollowersInTweet(jsonTweet) > 10000
        );
        filteredStream.to("important_tweets");

        final KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.start();
    }

    public static Integer extractFollowersInTweet(final String tweetJson) {
        final JsonElement userElement = JsonParser.parseString(tweetJson)
                .getAsJsonObject()
                .get("user");
        if(userElement != null) {
            final JsonElement followersElement = userElement.getAsJsonObject().get("followers_count");
            if(followersElement != null) return followersElement.getAsInt();
        }
        return 0;
    }
}
