package org.example.websitesalephone.spe;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.example.websitesalephone.dto.order.OrderByUserRequest;
import org.example.websitesalephone.dto.order.OrderSearch;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderSpecification {

    public static Specification<Order> search(OrderSearch search) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (search.getStatus() != null && !search.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), search.getStatus()));
            }

            if (search.getSearchText() != null && !search.getSearchText().isEmpty()) {
                String text = "%" + search.getSearchText() + "%";

                Join<Order, User> userJoin = root.join("customer", JoinType.LEFT);

                predicates.add(
                        cb.or(
                                cb.like(root.get("orderCode"), text),
                                cb.like(userJoin.get("fullName"), text),
                                cb.like(userJoin.get("phone"), text)
                        )
                );
            }

            Objects.requireNonNull(query).orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Order> search(OrderByUserRequest search) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (search.getId() != null && !search.getId().isEmpty()) {
                predicates.add(cb.equal(root.get("customer").get("id"), search.getId()));
            }

            // status
            if (search.getStatus() != null && !search.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), search.getStatus()));
            }

            // search text
            if (search.getSearchText() != null && !search.getSearchText().isEmpty()) {
                String text = "%" + search.getSearchText() + "%";
                predicates.add(
                        cb.like(root.get("orderCode"), text)
                );
            }

            // fromDate & toDate (OffsetDateTime)
            OffsetDateTime from = search.getFromDate();
            OffsetDateTime to = search.getToDate();

            if (from != null && to != null) {
                predicates.add(
                        cb.between(root.get("createdAt"), from, to)
                );
            } else if (from != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), from)
                );
            } else if (to != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("createdAt"), to)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}

