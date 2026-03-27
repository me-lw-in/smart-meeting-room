package com.example.smartmeetingroom.service.device;

import com.example.smartmeetingroom.dto.device.DeviceDTO;
import com.example.smartmeetingroom.entity.Device;
import com.example.smartmeetingroom.repository.DeviceRepository;
import com.example.smartmeetingroom.repository.DeviceTypeRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class DeviceServiceImpl implements DeviceService{

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    public void addDevice(DeviceDTO dto){
        String deviceName = StringCapitalizeUtil.capitalizeEachWord(dto.getDeviceName());
        if ( deviceRepository.existsByDeviceNameAndDeviceTypeIdNot(deviceName, dto.getDeviceTypeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, deviceName + " is already exists of this type");
        }
        var deviceType = deviceTypeRepository.findById(dto.getDeviceTypeId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device type not found")
        );
        var room = meetingRoomRepository.findById(dto.getMeetingRoomId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting room not found")
        );

        var device = new Device();
        device.setDeviceName(deviceName);
        device.setDeviceType(deviceType);
        device.setRoom(room);
        deviceRepository.save(device);
    }
}
