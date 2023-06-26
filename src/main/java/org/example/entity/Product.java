package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.request.CreateProductRequest;

import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private Double productPrice;

    @Column(name = "product_quantity")
    private Double productQuantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    public Product(CreateProductRequest createProductRequest) {
        this.productName = createProductRequest.getProductName();
        this.productPrice = createProductRequest.getProductPrice();
        this.productQuantity = createProductRequest.getProductQuantity();
    }
}
