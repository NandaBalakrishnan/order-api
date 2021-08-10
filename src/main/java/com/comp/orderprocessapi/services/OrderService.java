package com.comp.orderprocessapi.services;


import com.comp.orderprocessapi.models.Order;

import java.util.Optional;

public interface OrderService {

    Optional<Order> getOrderStatus(String orderId);

}
