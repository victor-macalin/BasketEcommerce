package com.java.victor.BasketEcommerce.controller;

import com.java.victor.BasketEcommerce.client.response.PlatzProductResponse;
import com.java.victor.BasketEcommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/basket")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<PlatzProductResponse>> getAll () {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    @GetMapping("/{id}")
    public ResponseEntity<PlatzProductResponse> getProductsId (@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductId(id));
    }
}
