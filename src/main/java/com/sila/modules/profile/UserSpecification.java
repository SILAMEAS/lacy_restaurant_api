package com.sila.modules.profile;


import com.sila.modules.address.model.Address_;
import com.sila.modules.profile.model.User;
import com.sila.modules.profile.model.User_;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecification {
    public static Specification<User> search(String search) {
        if (search == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(root.get(User_.fullName), search + "%"),
                criteriaBuilder.like(root.get(User_.email), search + "%"),
                criteriaBuilder.like(root.get(User_.ADDRESSES).get(Address_.STREET), search + "%")
        );

    }

    public static Specification<User> hasOrderedFromRestaurant(Long restaurantId) {
        return (root, query, cb) -> {
            query.distinct(true); // prevent duplicate users

            Join<?, ?> orders = root.join("orders", JoinType.INNER);
            return cb.equal(orders.get("restaurant").get("id"), restaurantId);
        };
    }


}