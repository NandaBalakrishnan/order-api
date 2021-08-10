package com.comp.orderprocessapi.controller;

import com.comp.orderprocessapi.models.Order;
import com.comp.orderprocessapi.models.OrderStatus;
import com.comp.orderprocessapi.models.PlaceOrderResponse;
import com.comp.orderprocessapi.repository.MongoRepository;
import com.comp.orderprocessapi.services.OrderProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("orders")
public class OrderController {

    @Autowired
    OrderProducerService orderProducerService;

    @Autowired
    MongoRepository mongoRepository;

    @Autowired
    ObjectMapper objectMapper;
    private final static Logger logger = LogManager.getLogger(OrderController.class);

    @PostMapping(value="publish/order",consumes = {"application/json"},produces = {"application/json"})
    ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody Order order) {
        order.setOrderId(UUID.randomUUID().toString());
        logger.info("Processing order id: " + order.getOrderId() + "for user : " + order.getCustomerUserName());
        PlaceOrderResponse placeOrderResponse = orderProducerService.publishOrder(order);
        if (placeOrderResponse != null && placeOrderResponse.getStatus() == OrderStatus.PLACED) {
            return new ResponseEntity<>(placeOrderResponse, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(placeOrderResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path="order/status/{orderId}",produces = {"application/json"})
    ResponseEntity<ObjectNode> getOrderStatus(@PathVariable String orderId) {
        Optional<Order> order = mongoRepository.findById(orderId);
        if (order.isPresent()) {
            ObjectNode orderStatusResponse = objectMapper.createObjectNode();
            orderStatusResponse.put("orderId", order.get().getOrderId());
            orderStatusResponse.put("status", String.valueOf(order.get().getStatus()));
            return new ResponseEntity<>(orderStatusResponse, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
