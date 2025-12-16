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
@Table(name = "addresses")
public class Address extends BaseEntity{
    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String addressLine;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(length = 20)
    private String postalCode;

    private boolean isDefault;

    // getters and setters
}
