package com.java.victor.BasketEcommerce.client;

import com.java.victor.BasketEcommerce.client.response.PlatzProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "PlatziStoreClient", url = "${basket.client.url}", configuration = CustomErrorDecoder)
public interface  ProductClient {

    @GetMapping("/products")
    List<PlatzProductResponse> getAllProducts ();

    @GetMapping("/products/{id}")
   Optional<PlatzProductResponse> getById (@PathVariable Long id);
}
