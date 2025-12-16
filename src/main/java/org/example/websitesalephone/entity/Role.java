package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.websitesalephone.enums.RoleEnums;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity{

    @Id
    @Column(length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RoleEnums roleEnums;

    private Integer status;

    @OneToMany(mappedBy = "role")
    private List<User> users;

}
