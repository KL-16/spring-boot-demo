package org.example.service;

import org.example.entity.Cart;
import org.example.entity.Product;
import org.example.repository.CartRepository;
import org.example.repository.ProductRepository;
import org.example.request.CreateCartRequest;
import org.example.request.CreateProductRequest;
import org.example.request.UpdateCartRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    public List<Cart> getAllCarts() { return cartRepository.findAll();}

    public Cart getSingleCartById(long id) {
        if (cartRepository.existsById(id)) {
            Optional<Cart> optionalCart = cartRepository.findById(id);
            Cart cart = new Cart();
            cart.setTotalPrice(optionalCart.get().getTotalPrice());
            cart.setId(optionalCart.get().getId());
            cart.setAddedProducts(optionalCart.get().getAddedProducts());
            return cart;
        }
        else {
            throw new IllegalArgumentException("No cart with given ID exists");
        }
    }

    public Cart createCart (CreateCartRequest createCartRequest) {
        Cart cart = new Cart(createCartRequest);
        double totalPrice = 0d;
        List<Product> productList = new ArrayList<Product>();
        if (createCartRequest.getAddedProducts() != null) {
            for (CreateProductRequest createProductRequest :
                    createCartRequest.getAddedProducts()) {
                Product product = new Product();
                product.setProductName(createProductRequest.getProductName());
                product.setProductPrice(createProductRequest.getProductPrice());
                product.setProductQuantity(createProductRequest.getProductQuantity());
                product.setCart(cart);
                productList.add(product);
                totalPrice += createProductRequest.getProductPrice() * createProductRequest.getProductQuantity();
            }
        }
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
        productRepository.saveAll(productList);
        cart.setAddedProducts(productList);
        return cart;
    }

    public Cart addProduct (UpdateCartRequest updateCartRequest) {
        if (cartRepository.findById(updateCartRequest.getId()).isPresent()){
            Cart cart = cartRepository.findById(updateCartRequest.getId()).get();
            List<Product> productList = new ArrayList<Product>();
            if (updateCartRequest.getAddedProducts() != null) {
                for (CreateProductRequest addedProduct :
                        updateCartRequest.getAddedProducts()) {
                    if (addedProduct.getProductQuantity() <= 0) {
                        throw new IllegalArgumentException("Product quantity must be bigger than zero");
                    }
                    Product product = new Product(addedProduct);
                    product.setCart(cart);
                    productList.add(product);
                    cart.setTotalPrice(cart.getTotalPrice() + (addedProduct.getProductPrice() * addedProduct.getProductQuantity()));
                }
                productRepository.saveAll(productList);
            }

            cart.setAddedProducts(productList);
            cart = cartRepository.save(cart);
            return cart;
        }
        else {
            throw new IllegalArgumentException("No cart with given ID exists");
        }
    }
    public String addSingleProduct (long id) {
        String returnMessage;
        if(productRepository.findById(id).isPresent()) {
            Product product = productRepository.findById(id).get();
            long cartId = product.getCart().getId();
            if (cartRepository.findById(cartId).isPresent()) {
                Double addedProductPrice = product.getProductPrice();
                product.setProductQuantity(product.getProductQuantity() + 1);
                productRepository.save(product);
                Cart cart = cartRepository.findById(cartId).get();
                cart.setTotalPrice(cart.getTotalPrice() + addedProductPrice);
                cartRepository.save(cart);
                returnMessage = "Single product added to cart successfully";
            }
            else {
                throw new IllegalArgumentException("No cart with given ID exists");
            }
        }
        else {
            throw new IllegalArgumentException("Product does not exist");
        }
        return returnMessage;
    }


    public String deleteCart(long id) {
        if (cartRepository.findById(id).isPresent()) {
            Integer numberOfDeletedProducts = clearCart(id);
            cartRepository.deleteById(id);
            return "Cart with " + numberOfDeletedProducts + " products has been deleted successfully";
        } else {
            throw new IllegalArgumentException("No cart with given ID exists");
        }
    }

    public Integer clearCart(long cart_id) {
        return productRepository.deleteProductsByCartId(cart_id);
    }

    public void setTotalPriceToZero(long id) {
        if (cartRepository.findById(id).isPresent()) {
            Cart cart = cartRepository.findById(id).get();
            cart.setTotalPrice(0.00d);
            cartRepository.save(cart);
        }
        else{
            throw new IllegalArgumentException("No cart with given ID exists");
        }
    }

    public String deleteProductFromCart(long id) {
        if (productRepository.findById(id).isPresent()){
            Product product = productRepository.findById(id).get();
            long cartId = product.getCart().getId();
            if (cartRepository.findById(cartId).isPresent()) {
                productRepository.deleteById(id);
                Double removedProductPrice = product.getProductPrice() * product.getProductQuantity();
                Cart cart = cartRepository.findById(cartId).get();
                cart.setTotalPrice(cart.getTotalPrice() - removedProductPrice);
                cartRepository.save(cart);
                return "Product removed from cart successfully";
            }
            else {
                throw new IllegalArgumentException("No cart with given ID exists");
            }
        }
        else {
            throw new IllegalArgumentException("Product does not exist");
        }
    }

    public String deleteSingleProductFromCart(long id) {
        if(productRepository.findById(id).isPresent()) {
            Product product = productRepository.findById(id).get();
            String returnMessage;
            if(product.getProductQuantity() == 1) {
                returnMessage = deleteProductFromCart(id);
            }
            else {
                long cartId = product.getCart().getId();
                if (cartRepository.findById(cartId).isPresent()) {
                    Double removedProductPrice = product.getProductPrice();
                    product.setProductQuantity(product.getProductQuantity() - 1);
                    productRepository.save(product);
                    Cart cart = cartRepository.findById(cartId).get();
                    cart.setTotalPrice(cart.getTotalPrice() - removedProductPrice);
                    cartRepository.save(cart);
                    returnMessage = "Single product removed from cart successfully";
                }
                else {
                    throw new IllegalArgumentException("No cart with given ID exists");
                }
            }
            return returnMessage;
        }
        else {
            throw new IllegalArgumentException("Product does not exist");
        }
    }
}
