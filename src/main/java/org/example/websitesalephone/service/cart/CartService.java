package org.example.websitesalephone.service.cart;

import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.cart.CartRequest;
import org.example.websitesalephone.dto.cart.CartSearch;
import org.example.websitesalephone.dto.cart.CheckOutRequest;
import org.example.websitesalephone.dto.cart.BuyNowCartRequest;

public interface CartService {

    CommonResponse addToCart(CartRequest request);

    CommonResponse updateCartItem(CartRequest request);

    CommonResponse getCartItems(CartSearch search);

    CommonResponse checkoutCart(CheckOutRequest checkOutRequest);

    CommonResponse buyNow(BuyNowCartRequest buyNowCartRequest);
}
