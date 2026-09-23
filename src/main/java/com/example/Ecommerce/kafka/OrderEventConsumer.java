package com.example.Ecommerce.kafka;

import com.example.Ecommerce.dto.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    @KafkaListener(
            topics = "order-created",
            groupId = "ecommerce-group"
    )
    public void consumeOrderCreated(OrderCreatedEvent event) {

        System.out.println("========== ORDER CREATED ==========");
        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("User ID: " + event.getUserId());
        System.out.println("Total Amount: " + event.getTotalAmount());
        System.out.println("==================================");
    }
}