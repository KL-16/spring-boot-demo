package org.example.service;

import lombok.AllArgsConstructor;
import org.example.entity.Cart;
import org.example.entity.Product;
import org.example.repository.CartRepository;
import org.example.repository.ProductRepository;
import org.example.request.CreateCartRequest;
import org.example.request.CreateProductRequest;
import org.example.request.UpdateCartRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    public Cart getSingleCartById(Long id) {
        return cartRepository.findById(id)
                .map(savedCart -> Cart.builder()
                        .addedProducts(savedCart.getAddedProducts())
                        .totalPrice(savedCart.getTotalPrice())
                        .id(savedCart.getId())
                        .build())
                .orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
    }

    public Cart createCart(CreateCartRequest createCartRequest) {
        var cart = new Cart(createCartRequest);
        var totalPrice = new AtomicReference<>(new BigDecimal("0.0"));
        List<Product> products = Optional.ofNullable(createCartRequest.getAddedProducts())
                .map(productList -> productList.stream()
                        .map(createProductRequest -> buildProduct(cart, totalPrice, createProductRequest))
                        .collect(Collectors.toList())).orElse(new ArrayList<>());
        cart.setTotalPrice(totalPrice.get());
        cartRepository.save(cart);
        productRepository.saveAll(products);
        cart.setAddedProducts(products);
        return cart;
    }

    private static Product buildProduct(Cart cart, AtomicReference<BigDecimal> totalPrice, CreateProductRequest createProductRequest) {
        totalPrice.updateAndGet(v -> v.add(createProductRequest.getProductPrice().multiply(createProductRequest.getProductQuantity())));
        return Product.builder().productName(createProductRequest.getProductName())
                .productPrice(createProductRequest.getProductPrice())
                .productQuantity(createProductRequest.getProductQuantity())
                .cart(cart)
                .build();
    }

    public Cart addProduct(UpdateCartRequest updateCartRequest) {
        return cartRepository.findById(updateCartRequest.getId()).map(cart1 -> {
            AtomicReference<BigDecimal> totalPrice = new AtomicReference<>(cart1.getTotalPrice());
            List<Product> products = Optional.ofNullable(updateCartRequest.getAddedProducts())
                    .map(productList -> productList.stream()
                            .map(createProductRequest -> {
                                //wydzielic klamry do metod
                                if (createProductRequest.getProductQuantity().compareTo(new BigDecimal("0.0")) <= 0) {
                                    throw new IllegalArgumentException("Product quantity must be bigger than zero");
                                }
                                totalPrice.updateAndGet(v -> v.add(createProductRequest.getProductPrice()
                                        .multiply(createProductRequest.getProductQuantity())));
                                return Product.builder()
                                        .productName(createProductRequest.getProductName())
                                        .productQuantity(createProductRequest.getProductQuantity())
                                        .productPrice(createProductRequest.getProductPrice())
                                        .cart(cart1)
                                        .build();
                            }).collect(Collectors.toList())).orElse(new ArrayList<>());
            productRepository.saveAll(products);
            cart1.setAddedProducts(products);
            //sprobowac czy save cart jest potrzebne tutaj (return u gory powinien sam zrobic save'a)
            return cartRepository.save(cart1);
        }).orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
    }

    public String addSingleProduct(Long id) {
        return productRepository.findById(id).map(product1 -> {
            Long cartId = product1.getCart().getId();
            Optional<Cart> cart = cartRepository.findById(cartId);
            cart.map(cart1 -> {
                //wydzielic klamre
                BigDecimal addedProductPrice = product1.getProductPrice();
                product1.setProductQuantity(product1.getProductQuantity().add(new BigDecimal("1.0")));
                productRepository.save(product1);
                cart1.setTotalPrice(new AtomicReference<>(cart1.getTotalPrice()).get().add(addedProductPrice));
                cartRepository.save(cart1);
                return cart1;
            }).orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
            return "Single product added to cart successfully";
        }).orElseThrow(() -> new RuntimeException("Product does not exist"));
    }


    public String deleteCart(Long id) {
        return cartRepository.findById(id).map(cart1 -> {
            cartRepository.deleteById(id);
            return "Cart deleted successfully";
        }).orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
    }

    public Integer clearCart(Long cartId) {
        return productRepository.deleteProductsByCartId(cartId);
    }

    public void setTotalPriceToZero(Long id) {
        //analogicznie
        Optional<Cart> cart = cartRepository.findById(id);
        cart.map(cart1 -> {
            cart1.setTotalPrice(new BigDecimal("0.0"));
            return cartRepository.save(cart1);
        }).orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
    }

    public String deleteProductFromCart(Long id) {
        Optional<Product> product = productRepository.findById(id);
        product.map(product1 -> {
            Long cartId = product1.getCart().getId();
            Optional<Cart> cart = cartRepository.findById(cartId);
            cart.map(cart1 -> {
                BigDecimal removedProductPrice = product1.getProductPrice().multiply(product1.getProductQuantity());
                cart1.setTotalPrice(new AtomicReference<>(cart1.getTotalPrice()).get().subtract(removedProductPrice));
                cartRepository.save(cart1);
                productRepository.deleteById(id);
                return cart1;
            }).orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
            return product1;
        }).orElseThrow(() -> new RuntimeException("Product does not exist"));
        return "Product removed from cart successfully";
    }

    public String deleteSingleProductFromCart(Long id) {
        Optional<Product> product = productRepository.findById(id);
        product.map(product1 -> {
            if (product1.getProductQuantity().equals(new BigDecimal("1.0"))) {
                return deleteProductFromCart(id);
            } else {
                Long cartId = product1.getCart().getId();
                Optional<Cart> cart = cartRepository.findById(cartId);
                cart.map(cart1 -> {
                    BigDecimal removedProductPrice = product1.getProductPrice();
                    product1.setProductQuantity(product1.getProductQuantity().subtract(new BigDecimal("1.0")));
                    productRepository.save(product1);
                    cart1.setTotalPrice(new AtomicReference<>(cart1.getTotalPrice()).get().subtract(removedProductPrice));
                    cartRepository.save(cart1);
                    return cart1;
                }).orElseThrow(() -> new RuntimeException("No cart with given ID exists"));
            }
            return product1;
        }).orElseThrow(() -> new RuntimeException("Product does not exist"));
        return "Single product removed from cart successfully";
    }
}
