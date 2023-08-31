package org.example.controller;

import jakarta.validation.Valid;
import org.example.entity.Cart;
import org.example.request.CreateCartRequest;
import org.example.request.UpdateCartRequest;
import org.example.response.CartResponse;
import org.example.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cart/")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping()
    public List<CartResponse> getAllCarts() {
        List<CartResponse> cartResponseList = new ArrayList<>();
        cartService.getAllCarts().forEach(cart -> {
            cartResponseList.add(new CartResponse(cart));
        });
        return cartResponseList;
    }

    @GetMapping("{cartId}")
    public CartResponse getSingleCartById(@PathVariable Long cartId) {
        Cart cart = cartService.getSingleCartById(cartId);
        return new CartResponse(cart);
    }

    @PostMapping()
    public CartResponse createCart(@Valid @RequestBody CreateCartRequest createCartRequest) {
        Cart cart = cartService.createCart(createCartRequest);
        return new CartResponse(cart);
    }

    @PutMapping()
    public CartResponse addProduct(@Valid @RequestBody UpdateCartRequest updateCartRequest) {
        Cart cart = cartService.addProduct(updateCartRequest);
        return new CartResponse(cart);
    }

    @PutMapping("{productId}")
    public String addSingleProduct(@PathVariable Long productId) {
        return cartService.addSingleProduct(productId);
    }

    @DeleteMapping("{id}")
    public String deleteCart(@PathVariable Long id) {
        return cartService.deleteCart(id);
    }

    @DeleteMapping("clear/{id}")
    public String clearCart(@PathVariable Long id) {
        String returnMessage = cartService.clearCart(id) + " products have been removed";
        cartService.setTotalPriceToZero(id);
        return returnMessage;
    }

    @DeleteMapping("removeByIdProduct/{productId}")
    public String removeProductFromCart(@PathVariable Long productId) {
        return cartService.deleteProductFromCart(productId);
    }

    @DeleteMapping("removeByIdSingleProduct/{productId}")
    public String removeSingleProductFromCart(@PathVariable Long productId) {
        return cartService.deleteSingleProductFromCart(productId);
    }
}
