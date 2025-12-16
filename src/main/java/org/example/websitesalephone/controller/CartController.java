package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.cart.CartRequest;
import org.example.websitesalephone.dto.cart.CartSearch;
import org.example.websitesalephone.dto.cart.CheckOutRequest;
import org.example.websitesalephone.service.cart.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public CommonResponse addToCart(@RequestBody CartRequest request) {
        return cartService.addToCart(request);
    }

    @PutMapping("/update")
    public CommonResponse updateCartItem(@RequestBody CartRequest request) {
        return cartService.updateCartItem(request);
    }

    @PostMapping("/items")
    public CommonResponse getCartItems(@RequestBody CartSearch search) {
        return cartService.getCartItems(search);
    }

    @PostMapping("/checkout")
    public CommonResponse checkout(@RequestBody CheckOutRequest checkOutRequest) {
        return cartService.checkoutCart(checkOutRequest);
    }
}
