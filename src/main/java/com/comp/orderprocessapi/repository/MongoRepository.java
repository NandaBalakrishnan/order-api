package com.comp.orderprocessapi.repository;

import com.comp.orderprocessapi.models.Order;

public interface MongoRepository extends org.springframework.data.mongodb.repository.MongoRepository<Order, String> {
}
