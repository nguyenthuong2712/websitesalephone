package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ShopRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRegistrationRepository extends JpaRepository<ShopRegistration, String> {
    Optional<ShopRegistration> findFirstByUser_IdOrderByCreatedAtDesc(String userId);

    Optional<ShopRegistration> findByIdAndUser_Id(String shopId, String userId);
}
