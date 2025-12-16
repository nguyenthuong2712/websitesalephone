package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}
