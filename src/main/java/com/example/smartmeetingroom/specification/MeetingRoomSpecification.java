package com.example.smartmeetingroom.specification;

import com.example.smartmeetingroom.entity.MeetingRoom;
import com.example.smartmeetingroom.enums.RoomStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class MeetingRoomSpecification {
    public static Specification<MeetingRoom> getAllMeetingRooms(Integer floor,
                                                                RoomStatus meetingRoomStatus,
                                                                boolean includeDeleted,
                                                                boolean onlyDeleted) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<>();
            if (floor != null) {
                predicates.add(cb.equal(root.get("floor"), floor));
            }
            if (meetingRoomStatus != null) {
                predicates.add(cb.equal(root.get("status"),meetingRoomStatus));
            }
            if (onlyDeleted) {
                predicates.add(cb.equal(root.get("isDeleted"), true));
            } else if (!includeDeleted) {
                predicates.add(cb.equal(root.get("isDeleted"), false));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
