package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "origin")
public class Origin extends BaseEntity{

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "name", columnDefinition = "NVARCHAR(255) COLLATE Vietnamese_CI_AS")
    private String name;

    @OneToMany(mappedBy = "origin", cascade = CascadeType.ALL)
    private List<ProductVariant> productVariants;
}
