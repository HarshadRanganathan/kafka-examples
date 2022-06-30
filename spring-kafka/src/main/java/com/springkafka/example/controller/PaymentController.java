package com.springkafka.example.controller;

import com.springkafka.example.kafka.producer.PaymentProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProducer paymentProducer;

    @PostMapping
    public ResponseEntity<String> processPayment() {
        if(paymentProducer.processPayment(createPayment(), "payments")) {
            return ResponseEntity.ok("Created payment");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private example.avro.Payment createPayment() {
        return example.avro.Payment.newBuilder()
                .setName("Harshad")
                .setAmount(100.00F)
                .setBalance(500.00F)
                .setIban("IBAN100453455363")
                .setToIban("IBAN100453451123")
                .setProcessed(true)
                .build();
    }
}
