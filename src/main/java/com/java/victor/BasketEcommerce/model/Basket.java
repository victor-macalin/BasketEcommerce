package com.java.victor.BasketEcommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
@Builder
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class Basket {
    @Id
    private String id;
    private Long client;
    private BigDecimal precoTotal;
    List<Product> produtos;
    private StatusBasket status;

    public void calcularPrecoTotal (){
       this.precoTotal = produtos.stream()
                .map(product ->  product.getPrice().multiply(BigDecimal.valueOf(product.getQuantidade())) )
               .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
