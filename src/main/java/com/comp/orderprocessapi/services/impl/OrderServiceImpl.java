package com.comp.orderprocessapi.services.impl;

import com.comp.orderprocessapi.models.Order;
import com.comp.orderprocessapi.repository.MongoRepository;
import com.comp.orderprocessapi.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);

    @Autowired
    MongoRepository mongoRepository;

    @Override
    public Optional<Order> getOrderStatus(String orderId) {
      return mongoRepository.findById(orderId);
    }
}
