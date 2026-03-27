package com.example.smartmeetingroom.service.devicetype;

import com.example.smartmeetingroom.dto.devicetype.DeviceTypeDTO;
import com.example.smartmeetingroom.entity.DeviceType;
import com.example.smartmeetingroom.repository.DeviceTypeRepository;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class DeviceTypeServiceImpl implements DeviceTypeService {

    private final DeviceTypeRepository deviceTypeRepository;

    public void addDeviceType(DeviceTypeDTO dto){
        String deviceType = StringCapitalizeUtil.capitalizeEachWord(dto.getDeviceType());
        if (deviceTypeRepository.existsByName(deviceType)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, deviceType + " already exists");
        }

        var deviceTypeObj = new DeviceType();
        deviceTypeObj.setName(deviceType);
        deviceTypeRepository.save(deviceTypeObj);
    }
}
