package org.example.websitesalephone.spe;

import jakarta.persistence.criteria.Predicate; // 🔥 QUAN TRỌNG
import org.apache.logging.log4j.util.Strings;
import org.example.websitesalephone.dto.user.UserSearchForm;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.RoleEnums;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> search(UserSearchForm searchForm, User loginUser) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // searchText
            if (Strings.isNotEmpty(searchForm.getSearchText())) {
                predicates.add(
                        cb.or(
                                cb.like(root.get("username"), searchForm.getSearchText()),
                                cb.like(root.get("email"), searchForm.getSearchText())
                        )
                );
            }

            // role STAFF → chỉ xem cùng role
            if (RoleEnums.STAFF.getValue().equalsIgnoreCase(searchForm.getRole())) {
                predicates.add(cb.equal(root.get("role"), loginUser.getRole()));
            }

            // sort createdAt DESC
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
