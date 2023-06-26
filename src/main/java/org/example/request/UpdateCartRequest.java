package org.example.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.entity.Product;

import java.util.List;

@Getter
@Setter
@Builder
public class UpdateCartRequest {
    @NotNull(message = "cart ID is required") //validation but for numeric type
    private long id;

    private Double totalPrice;
    private List<CreateProductRequest> addedProducts;
}
