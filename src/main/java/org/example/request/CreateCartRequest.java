package org.example.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CreateCartRequest {

    private BigDecimal totalPrice;
    private List<CreateProductRequest> addedProducts;
}
