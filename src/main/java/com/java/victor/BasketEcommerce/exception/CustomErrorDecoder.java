package com.java.victor.BasketEcommerce.exception;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new DataNotFoundExeption("Produto nao encontrado");
            default:
                return new Exception("excessao enquanto escolhia produto");
        }

    }
}
