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

    @Autowired
    CartController cartController;

    @MockBean
    CartService cartService;

    ObjectMapper objectMapper = new ObjectMapper();

    final static long product_id = 1L;
    final static long cart_id_2 = 2L;
    final static long cart_id_3 = 3L;

    private CreateProductRequest getProductsRequests(String name, double price, double quantity) {
        CreateProductRequest createProductRequest = new CreateProductRequest();
        createProductRequest.setProductName(name);
        createProductRequest.setProductPrice(price);
        createProductRequest.setProductQuantity(quantity);
        return createProductRequest;
    }

    private Cart getCart(String name1, double price1, double quantity1,
                         String name2, double price2, double quantity2) {
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        CreateProductRequest createProductRequest1 = getProductsRequests(name1, price1, quantity1);
        CreateProductRequest createProductRequest2 = getProductsRequests(name2, price2, quantity2);
        List<CreateProductRequest> productList = new ArrayList<>();
        productList.add(createProductRequest1);
        productList.add(createProductRequest2);
        createCartRequest.setAddedProducts(productList);
        createCartRequest.setTotalPrice(price1 * quantity1 + price2 * quantity2);
        return new Cart(createCartRequest);
    }

    private List<Cart> getTwoCarts () {
        Cart cart1 = getCart("Orange", 1.5d, 3d,
                "Banana", 1.5d, 1d);

        Cart cart2 = getCart("Cherry", 2d, 2d,
                "Apple", 0.5d, 3d);
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
        MvcResult mvcResult = mockMvc.perform(get("/api/cart/getAllCarts")).andReturn();
        var carts = Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse[].class));

        assertEquals(2, carts.size());
        assertEquals(6d, carts.get(0).getTotalPrice());
        assertEquals(cart_id_2, carts.get(0).getId());
        assertEquals(5.5d, carts.get(1).getTotalPrice());
        assertEquals(cart_id_3, carts.get(1).getId());
    }

    @Test
    void shouldGetSingleCartById() throws Exception {
        Mockito.when(cartService.getSingleCartById(cart_id_2)).thenReturn(getCart("Orange", 1.5d, 3d,
                "Banana", 1.5d, 1d));
        MvcResult mvcResult = mockMvc.perform(get("/api/cart/getSingleCartById/" + cart_id_2)).andReturn();
        var cart = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse.class);
        Assertions.assertNotNull(cart);
        assertEquals(6d, cart.getTotalPrice());
    }

    @Test
    void getSingleCartByIdShouldThrow() throws Exception {
        Mockito.when(cartService.getSingleCartById(cart_id_2)).thenThrow(new IllegalArgumentException("No cart with given ID exists"));
        try {
            mockMvc.perform(get("/api/cart/getSingleCartById/" + cart_id_2));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void shouldCreateCart() throws Exception {
        Mockito.when(cartService.createCart(ArgumentMatchers.any()))
                .thenReturn(getCart("Orange", 1.5d, 3d,
                "Banana", 1.5d, 1d));

        List<CreateProductRequest> productList = new ArrayList<>();
        productList.add(getProductsRequests("Orange", 1.5d, 3d));
        productList.add(getProductsRequests("Banana", 1.5d, 1d));
        MvcResult mvcResult = mockMvc.perform(post("/api/cart/createCart")
                        .content(objectMapper.writeValueAsBytes(CreateCartRequest.builder()
                                        .addedProducts(productList)
                                        .totalPrice(6.0d)
                                .build()))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var postedCart = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse.class);
        assertEquals(6.0d, postedCart.getTotalPrice());
        assertEquals(2, postedCart.getAddedProducts().size());
        assertEquals("Orange", postedCart.getAddedProducts().get(0).getProductName());
        assertEquals(3d, postedCart.getAddedProducts().get(0).getProductQuantity());
        assertEquals(1.5d, postedCart.getAddedProducts().get(0).getProductPrice());
        assertEquals("Banana", postedCart.getAddedProducts().get(1).getProductName());
        assertEquals(1d, postedCart.getAddedProducts().get(1).getProductQuantity());
        assertEquals(1.5d, postedCart.getAddedProducts().get(1).getProductPrice());
    }

    @Test
    void shouldAddProduct() throws Exception {
        Mockito.when(cartService.addProduct(ArgumentMatchers.any()))
                .thenReturn(getCart("Pineapple", 2.5d, 2d,
                        "Cherry", 2.0d, 1d));

        CreateProductRequest createProductRequest1 = new CreateProductRequest();
        createProductRequest1.setProductQuantity(2d);
        createProductRequest1.setProductPrice(2.5d);
        createProductRequest1.setProductName("Pineapple");
        Product product1 = new Product(createProductRequest1);
        ProductResponse productResponse1 = new ProductResponse(product1);

        CreateProductRequest createProductRequest2 = new CreateProductRequest();
        createProductRequest2.setProductQuantity(1d);
        createProductRequest2.setProductPrice(2.0d);
        createProductRequest2.setProductName("Cherry");
        Product product2 = new Product(createProductRequest2);
        ProductResponse productResponse2 = new ProductResponse(product2);

        MvcResult mvcResult = mockMvc.perform(put("/api/cart/addProduct")
                        .content(objectMapper.writeValueAsBytes(UpdateCartRequest.builder()
                                .totalPrice(6.0d)
                                .build()))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var updatedCart = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CartResponse.class);
        assertTrue(updatedCart.getAddedProducts().contains(productResponse1));
        assertTrue(updatedCart.getAddedProducts().contains(productResponse2));
    }

    @Test
    void addProductShouldThrowCartIllegal() throws Exception {
        Mockito.when(cartService.addProduct(any(UpdateCartRequest.class))).thenThrow(new IllegalArgumentException("No cart with given ID exists"));
        try {
            mockMvc.perform(put("/api/cart/addProduct")
                    .content(objectMapper.writeValueAsBytes(UpdateCartRequest.builder()
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
            mockMvc.perform(put("/api/cart/addProduct")
                            .content(objectMapper.writeValueAsBytes(UpdateCartRequest.builder()
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
        MvcResult mvcResult = mockMvc.perform(put("/api/cart/addSingleProduct/" + product_id)
                        .content(String.valueOf(product_id))
                        .contentType("application/json"))
                .andExpect(status().isOk()).andReturn();

        var returnedString = mvcResult.getResponse().getContentAsString();
        assertTrue(returnedString.contains("Single product added to cart successfully"));
    }

    @Test
    void addSingleProductShouldThrowCartIllegal() throws Exception {
        Mockito.when(cartService.addSingleProduct(product_id)).thenThrow(new IllegalArgumentException("No cart with given ID exists"));
        try {
            mockMvc.perform(put("/api/cart/addSingleProduct/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void addSingleProductShouldThrowProductIllegal() throws Exception {
        Mockito.when(cartService.addSingleProduct(product_id)).thenThrow(new IllegalArgumentException("Product does not exist"));
        try {
            mockMvc.perform(put("/api/cart/addSingleProduct/" + product_id));
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
        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/deleteCart/" + cart_id_2)
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
            mockMvc.perform(delete("/api/cart/deleteCart/" + cart_id_2));
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

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/clearCart/" + cart_id_2)
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
            mockMvc.perform(delete("/api/cart/clearCart/" + cart_id_2));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("No cart with given ID exists"));
        }
    }

    @Test
    void shouldRemoveByIdProductFromCart() throws Exception {
        Mockito.when(cartService.deleteProductFromCart(product_id))
                .thenReturn("Product removed from cart successfully");

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/removeByIdProductFromCart/" + product_id)
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
            mockMvc.perform(delete("/api/cart/removeByIdProductFromCart/" + product_id));
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
            mockMvc.perform(delete("/api/cart/removeByIdProductFromCart/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Product does not exist"));
        }
    }

    @Test
    void shouldRemoveByIdSingleProductFromCart() throws Exception {
        Mockito.when(cartService.deleteSingleProductFromCart(product_id))
                .thenReturn("Single product removed from cart successfully");

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/removeByIdSingleProductFromCart/" + product_id)
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

        MvcResult mvcResult = mockMvc.perform(delete("/api/cart/removeByIdSingleProductFromCart/" + product_id)
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
            mockMvc.perform(delete("/api/cart/removeByIdSingleProductFromCart/" + product_id));
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
            mockMvc.perform(delete("/api/cart/removeByIdSingleProductFromCart/" + product_id));
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Product does not exist"));
        }
    }
}
