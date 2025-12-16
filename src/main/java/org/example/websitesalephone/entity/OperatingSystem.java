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
@Table(name = "operating_system")
public class OperatingSystem extends BaseEntity{

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 100)
    private String name;

    private Boolean isDeleted;

//    @OneToMany(mappedBy = "operatingSystem", cascade = CascadeType.ALL)
//    private List<ProductVariant> productVariants;
}
