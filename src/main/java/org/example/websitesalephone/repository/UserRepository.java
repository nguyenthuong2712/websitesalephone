package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Role;
import org.example.websitesalephone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    List<User> findByUsername(String username);

    List<User> findByUsernameOrEmail(String username, String email);

    List<User> findByEmail(String email);

    Optional<User> findByUsernameAndIsDeleted(String loginId, Boolean isDeleted);

    Optional<User> findFirstByEmailAndIsDeleted(String email, boolean isDeleted);

    Optional<User> findByIdAndIsDeleted(String id, Boolean isDeleted);

    Page<User> findAllByIsDeleted(Boolean isDeleted, Pageable pageable);

    Page<User>  findByRoleAndIsDeleted(Role role, boolean b, PageRequest pageRequest);

    int countByIsDeletedFalse();
}
