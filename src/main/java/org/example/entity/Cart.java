package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.request.CreateCartRequest;
import org.example.request.CreateProductRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "cart_id")
    private Long id;

    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER)
    private List<Product> addedProducts;

    private BigDecimal totalPrice;

    public Cart (CreateCartRequest createCartRequest) {
        this.totalPrice = createCartRequest.getTotalPrice();
        addedProducts = new ArrayList<>();
        if(createCartRequest.getAddedProducts() != null) {
            for (CreateProductRequest createProductRequest : createCartRequest.getAddedProducts()) {
                addedProducts.add(new Product(createProductRequest));
            }
        }
    }
}
