package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.request.CreateCartRequest;
import org.example.request.CreateProductRequest;
import org.example.response.ProductResponse;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private long id;

    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER)
    private List<Product> addedProducts;

    @Column(name = "total_price")
    private Double totalPrice;

    public Cart (CreateCartRequest createCartRequest) {
        this.totalPrice = createCartRequest.getTotalPrice();
        addedProducts = new ArrayList<Product>();
        if(createCartRequest.getAddedProducts() != null) {
            for (CreateProductRequest createProductRequest : createCartRequest.getAddedProducts()) {
                addedProducts.add(new Product(createProductRequest));
            }
        }
    }
}
