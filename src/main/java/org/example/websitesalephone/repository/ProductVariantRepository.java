package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    Page<ProductVariant> findByProduct_NameLikeIgnoreCase(String nameProduct, Pageable pageable);

    List<ProductVariant> findByProduct_Id(String id);

    Optional<ProductVariant> findByProduct_IdAndOrigin_IdAndColor_IdAndRam_IdAndScreen_idAndCamera_id(
            String productId,
            String originId,
            String colorId,
            String ramId,
            String screenId,
            String cameraId
    );

    void deleteByProduct_Id(String productId);

    @Query("select pv.id from ProductVariant pv where pv.product.id = :productId")
    List<String> findIdsByProduct_Id(@Param("productId") String productId);

}
