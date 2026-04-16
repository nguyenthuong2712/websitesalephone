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
@Table(name = "suppliers")
public class Supplier extends BaseEntity{
    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 150)
    private String name;

    @Column(length = 100)
    private String contact;

    @OneToMany(mappedBy = "supplier")
    private List<Inventory> inventories;

    // getters & setters
}