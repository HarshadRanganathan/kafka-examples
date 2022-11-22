## Spring-Kafka

Run `mvn clean install` to generate the avro source classes in target directory.

Mark `generated-sources` as src directory in your IDE.

Run the Spring boot app.

### Schema Registry

To test compatibility against schema register server, before registering the schemas:

`mvn schema-registry:validate@validate`

To test compatibility against schema register server, before registering the schemas:

`mvn schema-registry:validate@test-compatibility`

To register schemas from the local file system on the target Schema Registry server:

`mvn schema-registry:register@register`

## Docs

<https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka>

<https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.integration>

<https://docs.spring.io/spring-kafka/docs/2.7.14/reference/html/>