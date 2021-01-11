# Twitter Producer & ElasticSearch Consumer

|Java Class|Functionality|
|---|---|
|TwitterProducer.java|Produces tweets from Twitter's streaming API to Kafka topic|
|ElasticSearchConsumer.java|Consumes tweets to ElasticSearch from Kafka topic|

## Steps to Run

1. Create a [Twitter Developer Account](https://developer.twitter.com/).

2. Create an [App](https://developer.twitter.com/en/apps/create) by providing a name for the app, description and details of how it will be used.

3. Once your app is approved, go to **Keys and tokens** section of the app to get your **Consumer API keys** & **Access token & access token secret**.

4. Paste these keys into respective variables in **TwitterProducer.java** class.

5. Run **TwitterProducer.java** class to consume tweets with terms **twitter, api** and produce them to Kafka topic **twitter_tweets**.

Note: If you are running the code within your corporate firewall, you may need to update the code to provide proxy connection details. Also, if you face any SSL verification issues/your firewall is blocking the requests you may get below error.
```
IOException caught when establishing connection to https://stream.twitter.com/1.1/statuses/filter.json?delimited=length&stall_warnings=true
```

6. Download [Elasticsearch](https://www.elastic.co/downloads/elasticsearch) to your local and run it in default port.

7. Run **ElasticSearchConsumer.java** class to consume tweets from Kafka topic **twitter_tweets** and publish them to your local ES server.

8. You can query the index **twitter** in your local ES server to view the tweets.

Alternatively, you can publish to a managed ES cluster as well.

Note: You will get below error when you try to publish more than 1000 tweets to your local ES server.

```
java.lang.IllegalArgumentException: Limit of total fields [1000] in index [twitter] has been exceeded
```

## Kafka Streams

Run `TweetsFilter.java` class to stream tweets from `twitter_tweets` topic and filter tweets with more than `10000` followers.