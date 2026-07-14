package com.java.victor.BasketEcommerce.repository;

import com.java.victor.BasketEcommerce.model.Basket;
import com.java.victor.BasketEcommerce.model.StatusBasket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BasketRepository extends MongoRepository<Basket, String > {
    Optional<Basket> findByClientAndStatus(Long client, StatusBasket statusBasket);
}
