package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}
