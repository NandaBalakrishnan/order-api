package com.comp.orderprocessapi.services.impl;

import com.comp.orderprocessapi.models.Order;
import com.comp.orderprocessapi.models.OrderStatus;
import com.comp.orderprocessapi.models.PlaceOrderResponse;
import com.comp.orderprocessapi.repository.MongoRepository;
import com.comp.orderprocessapi.services.OrderProducerService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.kafka.support.SendResult;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderProducerServiceImpl implements OrderProducerService {

    private final static Logger logger = LogManager.getLogger(OrderProducerServiceImpl.class);

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Autowired
    PlaceOrderResponse placeOrderResponse;

    @Autowired
    MongoRepository mongoRepository;

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;


    public PlaceOrderResponse publishOrder(Order order) {

        ListenableFuture<SendResult<String, Order>> future = kafkaTemplate.send(topic, order);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Order>>() {

            @Override
            public void onSuccess(SendResult<String, Order> result) {
                logger.info("Successfully placed order for ID: " + order.getOrderId() + " to processing queue");
                order.setStatus(OrderStatus.PLACED);
                mongoRepository.insert(order);
                placeOrderResponse.setStatus(OrderStatus.PLACED);
                placeOrderResponse.setMessage("Successfully placed order for processing");
            }

            @Override
            public void onFailure(@NotNull Throwable exception) {
                logger.error("Error while placing order with order ID: " + order.getOrderId() + " to processing queue. " + exception);
                order.setStatus(OrderStatus.FAILED);
                placeOrderResponse.setStatus(OrderStatus.FAILED);
                placeOrderResponse.setMessage("Failed to place order");
            }
        });

        return placeOrderResponse;
    }
}
