package org.example.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entity.Cart;
import org.example.entity.Product;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class CartResponse {

    private long id;

    private List<ProductResponse> addedProducts;

    private Double totalPrice;

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
}
