package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
}
