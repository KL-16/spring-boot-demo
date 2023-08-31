package org.example.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entity.Cart;
import org.example.entity.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class CartResponse {

    private Long id;

    private List<ProductResponse> addedProducts;

    private BigDecimal totalPrice;

    public CartResponse(Cart cart) {
        this.id = cart.getId();
        this.totalPrice = cart.getTotalPrice();
        if(cart.getAddedProducts() != null) {
            addedProducts = new ArrayList<ProductResponse>();
            for (Product product : cart.getAddedProducts()) {
                addedProducts.add(new ProductResponse(product));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartResponse that = (CartResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(addedProducts, that.addedProducts) && Objects.equals(totalPrice, that.totalPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, addedProducts, totalPrice);
    }
}
