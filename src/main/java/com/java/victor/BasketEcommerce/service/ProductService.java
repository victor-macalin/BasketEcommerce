package com.java.victor.BasketEcommerce.service;

import com.java.victor.BasketEcommerce.client.ProductClient;
import com.java.victor.BasketEcommerce.client.response.PlatzProductResponse;
import com.java.victor.BasketEcommerce.exception.DataNotFoundExeption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductClient productClient;


    @Cacheable(value = "products")
    public List<PlatzProductResponse> getAllProducts () {
        log.info("Pegando todos os produtos");
        return productClient.getAllProducts();
    }

    @Cacheable(value = "product", key = "#id")
    public PlatzProductResponse getProductId (Long id){
        log.info("pegando produto com id {}", id);
        return productClient.getById(id)
                .orElseThrow(() -> new DataNotFoundExeption("Produto nao encontrado"));
    }
}
