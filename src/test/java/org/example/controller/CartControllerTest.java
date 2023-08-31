package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Cart;
import org.example.entity.Product;
import org.example.request.CreateCartRequest;
import org.example.request.CreateProductRequest;
import org.example.request.UpdateCartRequest;
import org.example.response.CartResponse;
import org.example.response.ProductResponse;
import org.example.service.CartService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    ObjectMapper objectMapper = new ObjectMapper();

    final static Long product_id = 1L;
    final static Long cart_id_2 = 2L;
    final static Long cart_id_3 = 3L;

    private CreateProductRequest getProductsRequests(String name, BigDecimal price, BigDecimal quantity) {
        CreateProductRequest createProductRequest = new CreateProductRequest();
        createProductRequest.setProductName(name);
        createProductRequest.setProductPrice(price);
        createProductRequest.setProductQuantity(quantity);
        return createProductRequest;
    }

    private Cart getCart(String name1, BigDecimal price1, BigDecimal quantity1,
                         String name2, BigDecimal price2, BigDecimal quantity2) {
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        CreateProductRequest createProductRequest1 = getProductsRequests(name1, price1, quantity1);
        CreateProductRequest createProductRequest2 = getProductsRequests(name2, price2, quantity2);
        List<CreateProductRequest> productList = getCreateProductRequestsList(createProductRequest1, createProductRequest2);
        createCartRequest.setAddedProducts(productList);
        createCartRequest.setTotalPrice(price1.multiply(quantity1)
                .add(price2.multiply(quantity2)));
        return new Cart(createCartRequest);
    }

    private static List<CreateProductRequest> getCreateProductRequestsList(CreateProductRequest createProductRequest1, CreateProductRequest createProductRequest2) {
        List<CreateProductRequest> productList = new ArrayList<>();
        productList.add(createProductRequest1);
        productList.add(createProductRequest2);
        return productList;
    }

    private List<Cart> getTwoCarts() {
        Cart cart1 = getCart("Orange", new BigDecimal("1.5"), new BigDecimal("3.0"),
                "Banana", new BigDecimal("1.5"), new BigDecimal("1.0"));

        Cart cart2 = getCart("Cherry", new BigDecimal("2.0"), new BigDecimal("2.0"),
                "Apple", new BigDecimal("0.5"), new BigDecimal("3.0"));
        cart1.setId(cart_id_2);
        cart2.setId(cart_id_3);

        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart1);
        cartList.add(cart2);

        return cartList;
    }

    @Test
    void shouldGetAllCartsList() throws Exception {
        Mockito.when(cartService.getAllCarts()).thenReturn(getTwoCarts());
        MvcResult mvcResult = mockMvc.perform(get("/api/cart/")).andReturn();
        var carts = Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse[].class));

        List<CartResponse> referenceCarts = getCartResponses();

        assertEquals(2, carts.size());
        assertEquals(referenceCarts.get(0), carts.get(0));
        assertEquals(referenceCarts.get(1), carts.get(1));
    }

    private List<CartResponse> getCartResponses() {
        List<Cart> cartList = getTwoCarts();
        CartResponse cartResponse1 = new CartResponse(cartList.get(0));
        CartResponse cartResponse2 = new CartResponse(cartList.get(1));

        List<CartResponse> referenceCarts = new ArrayList<>();
        referenceCarts.add(cartResponse1);
        referenceCarts.add(cartResponse2);
        return referenceCarts;
    }

    @Test
    void shouldGetSingleCartById() throws Exception {
        Mockito.when(cartService.getSingleCartById(cart_id_2)).thenReturn(getCart("Orange",
                new BigDecimal("1.5"), new BigDecimal("3.0"),
                "Banana", new BigDecimal("1.5"), new BigDecimal("1.0")));
        MvcResult mvcResult = mockMvc.perform(get("/api/cart/" + cart_id_2)).andReturn();
        var cart = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse.class);
        CartResponse cartResponse = new CartResponse(getCart("Orange",
                new BigDecimal("1.5"), new BigDecimal("3.0"),
                "Banana", new BigDecimal("1.5"), new BigDecimal("1.0")));

        assertEquals(cartResponse, cart);
    }

    @Test
    void getSingleCartByIdShouldThrow() throws Exception {
        Mockito.when(cartService.getSingleCartById(cart_id_2)).thenThrow(new RuntimeException("No cart with given ID exists"));
        try {
            mockMvc.perform(get("/api/cart/" + cart_id_2));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void shouldCreateCart() throws Exception {
        Mockito.when(cartService.createCart(ArgumentMatchers.any()))
                .thenReturn(getCart("Orange", new BigDecimal("1.5"), new BigDecimal("3.0"),
                        "Banana", new BigDecimal("1.5"), new BigDecimal("1.0")));

        List<CreateProductRequest> productList = getCreateProductRequestsList(getProductsRequests("Orange", new BigDecimal("1.5"), new BigDecimal("3.0")), getProductsRequests("Banana", new BigDecimal("1.5"), new BigDecimal("1.0")));
        MvcResult mvcResult = mockMvc.perform(post("/api/cart/")
                        .content(objectMapper.writeValueAsBytes(CreateCartRequest.builder()
                                .addedProducts(productList)
                                .totalPrice(new BigDecimal("6.0"))
                                .build()))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var postedCart = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse.class);
        CartResponse cartResponse = new CartResponse(getCart("Orange", new BigDecimal("1.5"), new BigDecimal("3.0"),
                "Banana", new BigDecimal("1.5"), new BigDecimal("1.0")));

        assertEquals(cartResponse, postedCart);
    }

    @Test
    void shouldAddProduct() throws Exception {
        Cart cart = getCart("Pineapple", new BigDecimal("2.5"), new BigDecimal("2.0"),
                "Cherry", new BigDecimal("2.0"), new BigDecimal("1.0"));
        cart.setId(cart_id_2);
        Mockito.when(cartService.addProduct(ArgumentMatchers.any()))
                .thenReturn(cart);

        ProductResponse productResponse1 = getProductResponse("2.0", "2.5", "Pineapple");
        ProductResponse productResponse2 = getProductResponse("1.0", "2.0", "Cherry");

        MvcResult mvcResult = mockMvc.perform(put("/api/cart/")
                        .content(objectMapper.writeValueAsBytes(UpdateCartRequest.builder()
                                .totalPrice(new BigDecimal("7.0"))
                                        .id(cart_id_2)
                                .build()))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var updatedCart = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse.class);
        assertTrue(updatedCart.getAddedProducts().contains(productResponse1));
        assertTrue(updatedCart.getAddedProducts().contains(productResponse2));
    }

    private static ProductResponse getProductResponse(String val, String val1, String Pineapple) {
        CreateProductRequest createProductRequest1 = new CreateProductRequest();
        createProductRequest1.setProductQuantity(new BigDecimal(val));
        createProductRequest1.setProductPrice(new BigDecimal(val1));
        createProductRequest1.setProductName(Pineapple);
        Product product1 = new Product(createProductRequest1);
        return new ProductResponse(product1);
    }

    @Test
    void addProductShouldThrowCartIllegal() throws Exception {
        Mockito.when(cartService.addProduct(any(UpdateCartRequest.class))).thenThrow(new RuntimeException("No cart with given ID exists"));
        try {
            mockMvc.perform(put("/api/cart/")
                    .content(objectMapper.writeValueAsBytes(UpdateCartRequest.builder()
                                    .id(cart_id_2)
                            .build()))
                    .contentType("application/json"));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void addProductShouldThrowProductQuantityIllegal() throws Exception {
        Mockito.when(cartService.addProduct(any(UpdateCartRequest.class))).thenThrow(new IllegalArgumentException("Product quantity must be bigger than zero"));
        try {
            mockMvc.perform(put("/api/cart/")
                    .content(objectMapper.writeValueAsBytes(UpdateCartRequest.builder()
                                    .id(cart_id_2)
                            .build()))
                    .contentType("application/json"));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Product quantity must be bigger than zero"));
        }
    }

    @Test
    void shouldAddSingleProduct() throws Exception {
        Mockito.when(cartService.addSingleProduct(anyLong()))
                .thenReturn("Single product added to cart successfully");
        MvcResult mvcResult = mockMvc.perform(put("/api/cart/" + product_id)
                        .content(String.valueOf(product_id))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains("Single product added to cart successfully"));
    }

    @Test
    void addSingleProductShouldThrowCartIllegal() throws Exception {
        Mockito.when(cartService.addSingleProduct(product_id)).thenThrow(new RuntimeException("No cart with given ID exists"));
        try {
            mockMvc.perform(put("/api/cart/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void addSingleProductShouldThrowProductIllegal() throws Exception {
        Mockito.when(cartService.addSingleProduct(product_id)).thenThrow(new RuntimeException("Product does not exist"));
        try {
            mockMvc.perform(put("/api/cart/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Product does not exist"));
        }
    }

    @Test
    void shouldDeleteCart() throws Exception {
        int number_of_deleted_products = 3;
        Mockito.when(cartService.deleteCart(anyLong()))
                .thenReturn("Cart with " + number_of_deleted_products + " products has been deleted successfully");
        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/" + cart_id_2)
                        .content(String.valueOf(cart_id_2))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains("Cart with " + number_of_deleted_products + " products has been deleted successfully"));
    }

    @Test
    void deleteCartShouldThrowCartIllegal() throws Exception {
        Mockito.when(cartService.deleteCart(cart_id_2)).thenThrow(new IllegalArgumentException("No cart with given ID exists"));
        try {
            mockMvc.perform(delete("/api/cart/" + cart_id_2));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void shouldClearCart() throws Exception {
        int number_of_deleted_products = 3;
        Mockito.when(cartService.clearCart(cart_id_2))
                .thenReturn(number_of_deleted_products);

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/clear/" + cart_id_2)
                        .content(String.valueOf(cart_id_2))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains(number_of_deleted_products + " products have been removed"));
    }

    @Test
    void clearCartShouldThrowCartIllegal() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("No cart with given ID exists"))
                .when(cartService).setTotalPriceToZero(cart_id_2);

        try {
            mockMvc.perform(delete("/api/cart/clear/" + cart_id_2));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void shouldRemoveByIdProductFromCart() throws Exception {
        Mockito.when(cartService.deleteProductFromCart(product_id))
                .thenReturn("Product removed from cart successfully");

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/removeByIdProduct/" + product_id)
                        .content(String.valueOf(product_id))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains("Product removed from cart successfully"));
    }

    @Test
    void removeByIdProductFromCartShouldThrowCartIllegal() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("No cart with given ID exists"))
                .when(cartService).deleteProductFromCart(product_id);

        try {
            mockMvc.perform(delete("/api/cart/removeByIdProduct/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void removeByIdProductFromCartShouldThrowProductIllegal() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Product does not exist"))
                .when(cartService).deleteProductFromCart(product_id);

        try {
            mockMvc.perform(delete("/api/cart/removeByIdProduct/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Product does not exist"));
        }
    }

    @Test
    void shouldRemoveByIdSingleProductFromCart() throws Exception {
        Mockito.when(cartService.deleteSingleProductFromCart(product_id))
                .thenReturn("Single product removed from cart successfully");

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/removeByIdSingleProduct/" + product_id)
                        .content(String.valueOf(product_id))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains("Single product removed from cart successfully"));
    }

    @Test
    void shouldRemoveByIdLastProductFromCart() throws Exception {
        Mockito.when(cartService.deleteSingleProductFromCart(product_id))
                .thenReturn("Product removed from cart successfully");

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/removeByIdSingleProduct/" + product_id)
                        .content(String.valueOf(product_id))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains("Product removed from cart successfully"));
    }

    @Test
    void removeByIdSingleProductFromCartShouldThrowCartIllegal() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("No cart with given ID exists"))
                .when(cartService).deleteSingleProductFromCart(product_id);

        try {
            mockMvc.perform(delete("/api/cart/removeByIdSingleProduct/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void removeByIdSingleProductFromCartShouldThrowProductIllegal() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Product does not exist"))
                .when(cartService).deleteSingleProductFromCart(product_id);

        try {
            mockMvc.perform(delete("/api/cart/removeByIdSingleProduct/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Product does not exist"));
        }
    }
}
