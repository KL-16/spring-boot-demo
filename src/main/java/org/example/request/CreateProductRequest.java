package org.example.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    private String productName;

    private BigDecimal productPrice;

    private BigDecimal productQuantity;
}
