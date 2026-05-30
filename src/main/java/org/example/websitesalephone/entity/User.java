package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 150, nullable = false)
    private String username;

    @Column(length = 150, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String fullName;

    @Column(length = 100)
    private String codeUser;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String description;

    @Column(name = "address", columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String address;

    private String gender;

    @Basic
    @Column(name = "password_expired_at")
    private OffsetDateTime passwordExpiredAt;

    @Column(length = 500)
    private String avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<Order> orderStaffs;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Cart> carts;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

}
