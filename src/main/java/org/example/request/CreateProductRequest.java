package org.example.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

    private String productName;

    private Double productPrice;

    private Double productQuantity;
}
