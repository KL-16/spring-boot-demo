package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.request.CreateProductRequest;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String productName;

    private BigDecimal productPrice;

    private BigDecimal productQuantity;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    public Product(CreateProductRequest createProductRequest) {
        this.productName = createProductRequest.getProductName();
        this.productPrice = createProductRequest.getProductPrice();
        this.productQuantity = createProductRequest.getProductQuantity();
    }
}
