package org.example.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class UpdateCartRequest {
    @NotNull(message = "cart ID is required")
    private Long id;

    private BigDecimal totalPrice;
    private List<CreateProductRequest> addedProducts;
}
