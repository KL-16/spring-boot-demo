package org.example.service;

import org.example.entity.Cart;
import org.example.repository.CartRepository;
import org.example.request.CreateCartRequest;
import org.example.request.CreateProductRequest;
import org.example.request.UpdateCartRequest;
import org.example.response.CartResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CartServiceTest {

    final static Long made_up_id = 9999L;

    final static Long cart_id = 1L;

    @Autowired
    CartService cartService;

    @Test
    void newCreatedCartReturnsNotNullId() {
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        Cart cart = cartService.createCart(createCartRequest);
        CartResponse cartResponse = new CartResponse(cart);
        assertThat(cartResponse.getId(), notNullValue());
    }

    @Test
    void getCartByIdShouldThrowWhenIdDoesNotExist() {
        assertThrows(RuntimeException.class, () -> cartService.getSingleCartById(made_up_id));
    }

    @Test
    void newCreatedCartShouldHaveZeroTotalPrice() {
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        Cart cart = cartService.createCart(createCartRequest);
        CartResponse cartResponse = new CartResponse(cart);
        assertThat(new BigDecimal("0.0"), Matchers.comparesEqualTo(cartResponse.getTotalPrice()));
        cartService.deleteCart(cartResponse.getId());
    }

    @Test
    void newCreatedCartWithOneProductShouldReturnListOfProductsWithLengthOne() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);
        assertThat(cartResponse.getAddedProducts(), hasSize(1));
    }

    @Test
    void newCreatedCartWithOneProductShouldReturnQuantityEqualToOne() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);
        assertEquals(new BigDecimal("1.0"), cartResponse.getAddedProducts().get(0).getProductQuantity());
    }

    private CreateCartRequest GetCartWithNumberOfProducts(int numberOfProducts) {
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        List<CreateProductRequest> addedProducts = new ArrayList<CreateProductRequest>();
        if (numberOfProducts == 1) {
            CreateProductRequest createProductRequestApple = getApple();
            addedProducts.add(createProductRequestApple);
        } else {
            CreateProductRequest createProductRequestApple = getApple();
            addedProducts.add(createProductRequestApple);
            CreateProductRequest createProductRequestOrange = getOrange();
            addedProducts.add(createProductRequestOrange);
        }
        createCartRequest.setAddedProducts(addedProducts);
        return createCartRequest;
    }

    private static CreateProductRequest getApple() {
        CreateProductRequest createProductRequest = new CreateProductRequest();
        createProductRequest.setProductName("Apple");
        createProductRequest.setProductPrice(new BigDecimal("1.25"));
        createProductRequest.setProductQuantity(new BigDecimal("1.0"));
        return createProductRequest;
    }

    private static CreateProductRequest getOrange() {
        CreateProductRequest createProductRequest = new CreateProductRequest();
        createProductRequest.setProductName("Orange");
        createProductRequest.setProductPrice(new BigDecimal("2.5"));
        createProductRequest.setProductQuantity(new BigDecimal("2.0"));
        return createProductRequest;
    }

    @Test
    void addProductMethodShouldThrowWhenCartIdIsIncorrect() {
        UpdateCartRequest updateCartRequest = UpdateCartRequest.builder().build();
        updateCartRequest.setId(made_up_id);
        assertThrows(RuntimeException.class, () -> cartService.addProduct(updateCartRequest));

    }

    @Test
    void addProductMethodShouldThrowWhenQuantityIsEqualToZero() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        UpdateCartRequest updateCartRequest = UpdateCartRequest.builder().build();
        updateCartRequest.setId(cartResponse.getId());

        List<CreateProductRequest> productList = new ArrayList<>();
        productList.add(GetProductForProductList());
        productList.get(0).setProductQuantity(new BigDecimal("0.0"));
        updateCartRequest.setAddedProducts(productList);

        assertThrows(IllegalArgumentException.class, () -> cartService.addProduct(updateCartRequest));
    }

    @Test
    void addProductShouldReturnListOf2ProductsIf1AlreadyExisted() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        UpdateCartRequest updateCartRequest = UpdateCartRequest.builder().build();
        updateCartRequest.setId(cartResponse.getId());

        List<CreateProductRequest> productList = new ArrayList<>();
        productList.add(GetProductForProductList());
        updateCartRequest.setAddedProducts(productList);
        cartService.addProduct(updateCartRequest);

        Cart updatedCart = cartService.getSingleCartById(cart.getId());
        cartResponse = new CartResponse(updatedCart);
        assertThat(cartResponse.getAddedProducts(), hasSize(2));
    }

    private static CreateProductRequest GetProductForProductList() {
        CreateProductRequest createProductRequest = new CreateProductRequest();
        createProductRequest.setProductQuantity(new BigDecimal("1.0"));
        createProductRequest.setProductPrice(new BigDecimal("2.5"));
        createProductRequest.setProductName("Banana");
        return createProductRequest;
    }

    @Test
    void AfterAddProductCartShouldContainOneMoreProduct() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        UpdateCartRequest updateCartRequest = UpdateCartRequest.builder().build();
        updateCartRequest.setId(cartResponse.getId());

        List<CreateProductRequest> productList = new ArrayList<>();
        CreateProductRequest createProductRequest = GetProductForProductList();
        productList.add(createProductRequest);
        updateCartRequest.setAddedProducts(productList);
        cartService.addProduct(updateCartRequest);

        Cart updatedCart = cartService.getSingleCartById(cart.getId());
        cartResponse = new CartResponse(updatedCart);
        assertEquals(2, cartResponse.getAddedProducts().size());
    }

    @Test
    void addSingleProductShouldIncreaseQuantityByOne() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        long cartId = cartResponse.getId();
        long productId = cartResponse.getAddedProducts().get(0).getId();
        cartService.addSingleProduct(productId);

        cart = cartService.getSingleCartById(cartId);
        cartResponse = new CartResponse(cart);
        assertEquals(new BigDecimal("2.00"), cartResponse.getAddedProducts().get(0).getProductQuantity());
    }

    @Test
    void addSingleProductShouldIncreaseTotalPriceByNominalPriceOfProduct() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        long cartId = cartResponse.getId();
        long productId = cartResponse.getAddedProducts().get(0).getId();
        BigDecimal nominalPrice = cartResponse.getAddedProducts().get(0).getProductPrice();
        cartService.addSingleProduct(productId);

        cart = cartService.getSingleCartById(cartId);
        cartResponse = new CartResponse(cart);
        assertThat(nominalPrice.multiply(new BigDecimal("2.0")), Matchers.comparesEqualTo(cartResponse.getTotalPrice()));
    }

    @Test
    void addSingleProductShouldThrowWhenProductDoesNotExist() {
        assertThrows(RuntimeException.class, () -> cartService.addSingleProduct(made_up_id));
    }


    @Test
    void clearedCartShouldReturnListOfProductsWithZeroElements() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        long cartId = cartResponse.getId();
        cartService.clearCart(cartId);
        cart = cartService.getSingleCartById(cartId);
        cartResponse = new CartResponse(cart);
        assertThat(cartResponse.getAddedProducts(), hasSize(0));
        cartService.deleteCart(cartResponse.getId());
    }

    @Test
    void deletedCartShouldThrowWhenSearchedFor() {
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        Cart cart = cartService.createCart(createCartRequest);
        CartResponse cartResponse = new CartResponse(cart);
        long cartId = cartResponse.getId();
        cartService.deleteCart(cartId);
        assertThrows(RuntimeException.class, () -> cartService.getSingleCartById(cartId));
    }

    @Test
    void setTotalPriceToZeroShouldSetZeroTotalPrice() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        long cartId = cartResponse.getId();
        cartService.setTotalPriceToZero(cartId);
        cart = cartService.getSingleCartById(cartId);
        cartResponse = new CartResponse(cart);
        assertEquals(new BigDecimal("0.00"), cartResponse.getTotalPrice());
    }

    @Test
    void deleteProductFromCartShouldThrowIfProductDoesNotExist() {
        assertThrows(RuntimeException.class, () -> cartService.deleteProductFromCart(made_up_id));
    }

    @Test
    void deleteProductShouldReturnListOf0ProductsIf1AlreadyExisted() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(1));
        CartResponse cartResponse = new CartResponse(cart);

        Long productId = cartResponse.getAddedProducts().get(0).getId();
        Long cartId = cartResponse.getId();
        cartService.deleteProductFromCart(productId);

        cart = cartService.getSingleCartById(cartId);
        cartResponse = new CartResponse(cart);
        assertThat(cartResponse.getAddedProducts(), hasSize(0));
        cartService.deleteCart(cartResponse.getId());
    }

    @Test
    void deleteSingleProductShouldDecreaseQuantityByOne() {
        Cart cart = cartService.createCart(GetCartWithNumberOfProducts(2));
        CartResponse cartResponse = new CartResponse(cart);

        long productId = cartResponse.getAddedProducts().get(1).getId();
        long cartId = cartResponse.getId();
        cartService.deleteSingleProductFromCart(productId);

        cart = cartService.getSingleCartById(cartId);
        cartResponse = new CartResponse(cart);
        assertEquals(new BigDecimal("1.00"), cartResponse.getAddedProducts().get(1).getProductQuantity());
    }

    @Test
    void deleteSingleProductFromCartShouldThrowIfProductDoesNotExist() {
        assertThrows(RuntimeException.class, () -> cartService.deleteSingleProductFromCart(made_up_id));
    }

    @Test
    void getAllCartsShouldReturnCorrectSize() {
        List<Cart> allCartsList = getAllCarts();
        CartService cartServiceMock = mock(CartService.class);
        given(cartServiceMock.getAllCarts()).willReturn(allCartsList);
        List<Cart> cartList = new ArrayList<>();
        cartServiceMock.getAllCarts().forEach(cartList::add);
        assertThat(cartList, hasSize(2));
    }

    private List<Cart> getAllCarts() {
        Cart cart1 = new Cart();
        Cart cart2 = new Cart();
        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart1);
        cartList.add(cart2);
        return cartList;
    }

    @Test
    void getAllCartsShouldReturnEmptyList() {
        CartService cartServiceMock = mock(CartService.class);
        given(cartServiceMock.getAllCarts()).willReturn(List.of());
        List<Cart> cartList = new ArrayList<>();
        cartServiceMock.getAllCarts().forEach(cartList::add);
        assertThat(cartList, hasSize(0));
    }

    @Test
    void deleteByIdIsExpectedWhenDeletingCart() {
        CartService cartService = mock(CartService.class);
        cartService.clearCart(cart_id);
        verify(cartService).clearCart(cart_id);
    }

    @Test
    void getSingleCartShouldThrowException() {
        CartService cartService = mock(CartService.class);
        given(cartService.getSingleCartById(anyLong())).willThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> cartService.getSingleCartById(anyLong()));
    }

    @Test
    void deleteCartShouldThrowExceptionWhenCartIdDoesNotExist() {
        CartRepository cartRepository = mock(CartRepository.class);
        given(cartRepository.findById(cart_id)).willReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> cartService.deleteCart(cart_id));
    }

    @Test
    void createCartShouldUseCreateCartRequestTypeArgument() {
        CartService cartService = mock(CartService.class);
        ArgumentCaptor<CreateCartRequest> argumentCaptor = ArgumentCaptor.forClass(CreateCartRequest.class);
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        cartService.createCart(createCartRequest);
        verify(cartService).createCart(argumentCaptor.capture());
    }

    @Test
    void createCartShouldPassNullTotalPriceWithRequest() {
        ArgumentCaptor<CreateCartRequest> argumentCaptor = ArgumentCaptor.forClass(CreateCartRequest.class);
        CartService cartService = mock(CartService.class);
        CreateCartRequest createCartRequest = CreateCartRequest.builder().build();
        cartService.createCart(createCartRequest);
        verify(cartService).createCart(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().size(), equalTo(1));
        assertNull(argumentCaptor.getValue().getTotalPrice());
    }
}