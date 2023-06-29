package org.example.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entity.Product;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {

    private Long id;

    private String productName;

    private BigDecimal productPrice;

    private BigDecimal productQuantity;

    public ProductResponse (Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.productPrice = product.getProductPrice();
        this.productQuantity = product.getProductQuantity();
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        ProductResponse product = (ProductResponse) o;
//        return Objects.equals(productName, product.productName) && Objects.equals(productPrice, product.productPrice) && Objects.equals(productQuantity, product.productQuantity);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(productName, productPrice, productQuantity);
//    }
}
