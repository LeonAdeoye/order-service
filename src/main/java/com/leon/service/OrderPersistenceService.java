package com.leon.service;

import com.leon.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPersistenceService extends MongoRepository<Order, String> {
} 