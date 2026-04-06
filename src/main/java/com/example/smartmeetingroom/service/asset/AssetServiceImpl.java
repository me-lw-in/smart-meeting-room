package com.example.smartmeetingroom.service.asset;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetTypeRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@AllArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    public void addAsset(AssetDTO dto){
        Set<AssetStatus> ALLOWED_CREATE_STATUS  = Set.of(AssetStatus.AVAILABLE, AssetStatus.PENDING_INSTALLATION);
        var assetName = StringCapitalizeUtil.capitalizeEachWord(dto.getAssetName());
        var serialNumber = dto.getSerialNumber().trim().toUpperCase();
        if (!dto.getWarrantyExpiry().isAfter(dto.getPurchaseDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Warranty expiry must be after purchase date");
        }
        if (assetRepository.existsBySerialNumber(serialNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number already exists");
        }
        if (!ALLOWED_CREATE_STATUS.contains(dto.getAssetStatus())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status for asset creation");
        }
        if (assetRepository.existsByAssetNameAndAssetTypeIdNot(assetName, dto.getDeviceTypeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, assetName + " is already exists for other type");
        }
        var assetType = assetTypeRepository.findById(dto.getDeviceTypeId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset type not found")
        );
        var room = meetingRoomRepository.findById(dto.getMeetingRoomId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting room not found")
        );

        var asset = new Asset();
        asset.setAssetName(assetName);
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setWarrantyExpiry(dto.getWarrantyExpiry());
        asset.setRoom(room);
        asset.setAssetType(assetType);
        asset.setStatus(dto.getAssetStatus());
        assetRepository.save(asset);
    }

    @Transactional
    public void deleteAsset(Long assetId, Long meetingRoomId) {
        Asset asset;
        if (meetingRoomId == null) {
            asset = assetRepository.findById(assetId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found")
            );
        }else {
            asset = assetRepository.findByIdAndRoom_Id(assetId, meetingRoomId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found in the specified room")
            );
        }
       asset.setStatus(AssetStatus.RETIRED);
    }
}
