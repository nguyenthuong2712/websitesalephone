package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
}
