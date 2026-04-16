package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.websitesalephone.enums.ShopRegistrationStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shop_registrations")
public class ShopRegistration extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "shop_name", nullable = false, length = 255, columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String shopName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "description", columnDefinition = "NVARCHAR(1000) COLLATE Vietnamese_CI_AS")
    private String description;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "cccd_url", length = 500)
    private String cccdUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ShopRegistrationStatus status;

    @OneToMany(mappedBy = "shopRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopPaymentMethod> paymentMethods;
}
