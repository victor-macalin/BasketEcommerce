package com.java.victor.BasketEcommerce.service;

import com.java.victor.BasketEcommerce.client.request.BasketRequest;
import com.java.victor.BasketEcommerce.client.request.PagamentoRequest;
import com.java.victor.BasketEcommerce.client.response.PlatzProductResponse;
import com.java.victor.BasketEcommerce.exception.DataNotFoundExeption;
import com.java.victor.BasketEcommerce.model.Basket;
import com.java.victor.BasketEcommerce.model.Product;
import com.java.victor.BasketEcommerce.model.StatusBasket;
import com.java.victor.BasketEcommerce.repository.BasketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final ProductService productService;
    private final BasketRepository basketRepository;


    public Basket findBasketById (String id) {
       return basketRepository.findById(id)
               .orElseThrow(() -> new DataNotFoundExeption("basket nao encontrada"));
    };


    public Basket createBasket (BasketRequest basketRequest) {
        basketRepository.findByClientAndStatus(basketRequest.client(), StatusBasket.ABERTO)
                .ifPresent(basket -> {
                    throw  new IllegalArgumentException("Ja existe um carrinho aberto");
                });
        List<Product> produtos = new ArrayList<>();
       basketRequest.produtos().forEach(produtoRequest -> {
           PlatzProductResponse platzProductResponse = productService.getProductId(produtoRequest.id());
           produtos.add(Product.builder()
                           .id(platzProductResponse.id())
                           .nome(platzProductResponse.title())
                           .price(platzProductResponse.price())
                           .quantidade(produtoRequest.quantidade())
                           .build());
       });
        Basket basket = Basket
                .builder()
                .client(basketRequest.client())
                .produtos(produtos)
                .status(StatusBasket.ABERTO)
                .build();
        basket.calcularPrecoTotal();
      return   basketRepository.save(basket);
    }

    public Basket updateBasket (String id, BasketRequest basketRequest) {
        Basket basket =  findBasketById(id);
        List<Product> produtos = new ArrayList<>();
        basketRequest.produtos().forEach(productRequest -> {
            PlatzProductResponse platzProductResponse = productService.getProductId(productRequest.id());
            produtos.add(Product.builder()
                    .nome(platzProductResponse.title())
                    .price(platzProductResponse.price())
                    .quantidade(productRequest.quantidade())
                    .build());
        });
        basket.setProdutos(produtos);
        basket.calcularPrecoTotal();
        return basketRepository.save(basket);
    }

    public Basket pagamentoBasket (String id, PagamentoRequest pagamentoRequest) {
        Basket basket = findBasketById(id);
        basket.setMetodoDePagamento(pagamentoRequest.metodoDePagamento());
        basket.setStatus(StatusBasket.VENDIDO);
        return basketRepository.save(basket);
    }
    public void deleteBasketId ( String id) {
        basketRepository.delete(findBasketById(id));
    }
}
