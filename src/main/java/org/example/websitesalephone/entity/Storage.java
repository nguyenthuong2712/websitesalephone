package org.example.websitesalephone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "storage")
public class Storage extends BaseEntity{

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 50)
    private String name;

//    @OneToMany(mappedBy = "storage", cascade = CascadeType.ALL)
//    private List<ProductVariant> productVariants;

}
