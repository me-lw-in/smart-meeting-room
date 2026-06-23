package com.example.smartmeetingroom.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ParticipantsDTO {
    private Long meetingRoomID;
    private Integer totalCount;
}
