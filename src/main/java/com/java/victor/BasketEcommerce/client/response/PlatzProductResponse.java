package com.java.victor.BasketEcommerce.client.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PlatzProductResponse( Long id,  String title, BigDecimal price) {
}
