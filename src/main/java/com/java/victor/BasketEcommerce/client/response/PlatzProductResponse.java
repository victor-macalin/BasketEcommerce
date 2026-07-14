package com.java.victor.BasketEcommerce.client.response;

import java.io.Serializable;
import java.math.BigDecimal;

public record PlatzProductResponse( Long id,  String title, BigDecimal price) implements Serializable {
}
