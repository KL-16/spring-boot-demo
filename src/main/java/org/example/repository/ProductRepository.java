package org.example.repository;

import org.example.entity.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ProductRepository extends CrudRepository<Product, Long> {

    @Transactional
    Integer deleteProductsByCartId(Long cartId);
}
