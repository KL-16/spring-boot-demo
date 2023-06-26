package org.example.controller;

import jakarta.validation.Valid;
import org.example.entity.Cart;
import org.example.request.CreateCartRequest;
import org.example.request.UpdateCartRequest;
import org.example.response.CartResponse;
import org.example.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cart/")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("getAllCarts")
    public List<CartResponse> getAllCarts() {
        List<Cart> cartList = cartService.getAllCarts();
        List<CartResponse> cartResponseList = new ArrayList<CartResponse>();
        cartList.stream().forEach(cart -> {
            cartResponseList.add(new CartResponse(cart));
        });
        return cartResponseList;
    }

    @GetMapping("getSingleCartById/{cart_id}")
    public CartResponse getSingleCartById(@PathVariable long cart_id) {
        Cart cart = cartService.getSingleCartById(cart_id);
        return new CartResponse(cart);
    }

    @PostMapping("createCart")
    public CartResponse createCart(@Valid @RequestBody CreateCartRequest createCartRequest) {
        Cart cart = cartService.createCart(createCartRequest);
        return new CartResponse(cart);
    }

    // Has to be updated, will check if product is available and take it from new service (products)
    // It will take product_id as argument and will be renamed to createProduct
    @PutMapping("addProduct")
    public CartResponse addProduct (@Valid @RequestBody UpdateCartRequest updateCartRequest) {
        Cart cart = cartService.addProduct(updateCartRequest);
        return new CartResponse(cart);
    }

    // Logic will be changed, it will take in path cart_id/product_id
    // and will check if product is available
    @PutMapping("addSingleProduct/{product_id}")
    public String addSingleProduct (@PathVariable long product_id) {
        return cartService.addSingleProduct(product_id);
    }

    @DeleteMapping("deleteCart/{id}")
    public String deleteCart(@PathVariable long id) {
        return cartService.deleteCart(id);
    }

    @DeleteMapping("clearCart/{id}")
    public String clearCart(@PathVariable long id) {
        String returnMessage = cartService.clearCart(id) + " products have been removed";
        cartService.setTotalPriceToZero(id);
        return returnMessage;
    }

    @DeleteMapping("removeByIdProductFromCart/{product_id}")
    public String removeProductFromCart(@PathVariable long product_id) {
        return cartService.deleteProductFromCart(product_id);
    }

    @DeleteMapping("removeByIdSingleProductFromCart/{product_id}")
    public String removeSingleProductFromCart(@PathVariable long product_id) {
        return cartService.deleteSingleProductFromCart(product_id);
    }
}
