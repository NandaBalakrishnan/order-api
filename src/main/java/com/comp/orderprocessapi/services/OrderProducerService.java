package com.comp.orderprocessapi.services;


import com.comp.orderprocessapi.models.Order;
import com.comp.orderprocessapi.models.PlaceOrderResponse;

public interface OrderProducerService {
    PlaceOrderResponse publishOrder(Order order);
}
