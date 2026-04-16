package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ShopPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopPaymentMethodRepository extends JpaRepository<ShopPaymentMethod, String> {
}
