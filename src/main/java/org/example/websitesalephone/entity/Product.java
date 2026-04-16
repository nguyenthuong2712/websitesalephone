package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.websitesalephone.enums.ProductStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity{
    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "name", columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String description;

    @Column(name = "location", columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String location;

    @Column(name = "storage", columnDefinition = "NVARCHAR(100) COLLATE Vietnamese_CI_AS")
    private String storage;

    @Column(name = "device_make", columnDefinition = "NVARCHAR(100) COLLATE Vietnamese_CI_AS")
    private String deviceMake;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProductStatus status;

    @OneToMany(mappedBy = "product")
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product")
    private List<ProductImage> images;

}
