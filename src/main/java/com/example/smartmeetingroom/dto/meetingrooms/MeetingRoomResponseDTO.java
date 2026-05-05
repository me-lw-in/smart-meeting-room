package com.example.smartmeetingroom.dto.meetingrooms;

import com.example.smartmeetingroom.dto.asset.AssetCountDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingRoomResponseDTO {
    private Long roomId;
    private String roomName;
    private Integer floor;
    private Integer capacity;
    private String status;

    private Long totalDevices;
    private Long workingDevices;
    private Long nonWorkingDevices;

    private List<AssetCountDTO> assets;

    public MeetingRoomResponseDTO(Long roomId, String roomName, Integer floor, Integer capacity, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.floor = floor;
        this.capacity = capacity;
        this.status = status;
    }
}
