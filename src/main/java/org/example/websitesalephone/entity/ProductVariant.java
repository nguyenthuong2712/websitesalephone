package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity{
    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 50)
    private String sku;

    private BigDecimal price;

    private BigDecimal basePrice;

    private Integer quantity;

    private Integer quantityUnitSold;

    @OneToMany(mappedBy = "productVariant")
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "productVariant")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "productVariant")
    private List<CartItem> cartItems;

    @ManyToOne
    @JoinColumn(name = "ram_id", nullable = false)
    private Ram ram;

    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @ManyToOne
    @JoinColumn(name = "origin_id", nullable = false)
    private Origin origin;

    @ManyToOne
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;
}
