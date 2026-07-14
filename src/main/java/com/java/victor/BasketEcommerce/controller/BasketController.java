package com.java.victor.BasketEcommerce.controller;

import com.java.victor.BasketEcommerce.client.request.BasketRequest;
import com.java.victor.BasketEcommerce.client.response.PlatzProductResponse;
import com.java.victor.BasketEcommerce.model.Basket;
import com.java.victor.BasketEcommerce.model.Product;
import com.java.victor.BasketEcommerce.repository.BasketRepository;
import com.java.victor.BasketEcommerce.service.BasketService;
import com.java.victor.BasketEcommerce.service.ProductService;
import feign.FeignException;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/basket")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;
    private final BasketRepository basketRepository;
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Basket> getBasketById (@PathVariable String id) {
        try{
            return ResponseEntity.ok( basketService.findBasketById(id));
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping
    public ResponseEntity<Basket> createBasket (@RequestBody BasketRequest basketRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(basketService.createBasket(basketRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Basket> updateBasket (@PathVariable String id, @RequestBody BasketRequest basketRequest) {
        try {
            return ResponseEntity.ok(basketService.updateBasket(id, basketRequest));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }
    }
}
