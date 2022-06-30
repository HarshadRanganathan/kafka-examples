package com.springkafka.example.kafka.producer;

import example.avro.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<Account, SpecificRecordBase> kafkaTemplate;

    public boolean processPayment(final SpecificRecordBase payment, final String topic) {
        sendTransaction(topic, new Account(payment.get("name").toString(), payment.get("iban").toString()), payment);
        return true;
    }

    private void sendTransaction(final String topic, final Account key, final SpecificRecordBase message) {
        log.info("Going to process transaction, sending message {}", message);
        kafkaTemplate.send(topic, key, message);
    }
}
