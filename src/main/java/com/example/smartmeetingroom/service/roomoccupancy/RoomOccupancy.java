package com.example.smartmeetingroom.service.roomoccupancy;

import com.example.smartmeetingroom.dto.auth.LoginDTO;

public interface RoomOccupancy {

    void checkIn(LoginDTO dto, Long roomId);
}
