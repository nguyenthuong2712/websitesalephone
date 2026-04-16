package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shop_payment_methods")
public class ShopPaymentMethod extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne
    @JoinColumn(name = "shop_registration_id", nullable = false)
    private ShopRegistration shopRegistration;

    @Column(name = "method", length = 100, nullable = false)
    private String method;

    @Column(name = "qr_code", length = 500)
    private String qrCode;

    @Column(name = "note", columnDefinition = "NVARCHAR(500) COLLATE Vietnamese_CI_AS")
    private String note;
}
