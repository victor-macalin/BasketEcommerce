package com.java.victor.BasketEcommerce.client.request;
import java.util.List;

public record BasketRequest(
         Long client,
         List<ProductRequest>produtos

) {
}
