package com.example.smartmeetingroom.specification;

import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.enums.AssetStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;


public class AssetSpecification {
    public static Specification<Asset> getAssets(
            String search,
            Short typeId,
            Long meetingRoomId,
            boolean onlyDeleted,
            AssetStatus status
    ) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("room", JoinType.LEFT);
                root.fetch("assetType", JoinType.LEFT);
            }
            var predicates = new ArrayList<>();
            if (search != null && !search.isBlank()){
                predicates.add(
                        cb.like(cb.lower(root.get("assetName")),"%" + search.toLowerCase() + "%" )
                );
            }
            if (typeId != null) {
                predicates.add(
                        cb.equal(root.get("assetType").get("id"), typeId)
                );
            }
            if (meetingRoomId != null) {
                predicates.add(
                        cb.equal(root.get("room").get("id"), meetingRoomId)
                );
            }
            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }
            if (onlyDeleted) {
                predicates.add(cb.isTrue(root.get("isDeleted")));
            } else {
                predicates.add(cb.isFalse(root.get("isDeleted")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
