package com.springkafka.example.kafka.consumer;

import example.avro.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private Payment payment;

    @KafkaListener(topics = "payments", groupId = "payment-app")
    public void consume(final ConsumerRecord<?, ?> consumerRecord) {
        log.info("Received payload='{}'", consumerRecord.value());

        final Payment p = (Payment) consumerRecord.value();
        setPayment(p);
    }

    public Payment getPayment() {
        return payment;
    }

    private void setPayment(Payment payment) {
        this.payment = payment;
    }
}
