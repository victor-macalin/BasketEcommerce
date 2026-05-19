package com.java.victor.BasketEcommerce.service;

import com.java.victor.BasketEcommerce.client.ProductClient;
import com.java.victor.BasketEcommerce.client.response.PlatzProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductClient productClient;

    public List<PlatzProductResponse> getAllProducts () {
        return productClient.getAllProducts();
    }

    public PlatzProductResponse getProductId (Long id){
        return productClient.getById(id)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado"));
    }
}
