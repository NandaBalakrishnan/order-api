package com.comp.orderprocessapi.services;

import com.comp.orderprocessapi.models.Order;
import com.comp.orderprocessapi.models.OrderStatus;
import com.comp.orderprocessapi.repository.MongoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {
    private static final Logger logger = LogManager.getLogger(OrderConsumer.class);

    @Autowired
    private MongoRepository mongoRepository;

    @KafkaListener(topics = "${spring.kafka.template.default-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
            ,containerFactory = "orderKafkaListenerContainerFactory")
    public void consume(Order order){
        try {
            order.setStatus(OrderStatus.PROCESSED);
            logger.info("Processing order with ID: " + order.getOrderId());
            if (mongoRepository.findById(order.getOrderId()).isPresent()) {
                mongoRepository.save(order);
            }
           mongoRepository.insert(order);
        }
        catch (Exception e){
            logger.info("Failed to Update Order ID: " + order.getOrderId() + " to Database");
        }

    }
}
