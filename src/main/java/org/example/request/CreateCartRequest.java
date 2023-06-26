package org.example.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateCartRequest {

    private Double totalPrice;
    private List<CreateProductRequest> addedProducts;
}
