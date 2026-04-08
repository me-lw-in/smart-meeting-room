package com.example.smartmeetingroom.dto.meetingrooms;

import com.example.smartmeetingroom.dto.asset.AssetCountDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
}
