package com.example.smartmeetingroom.service.asset;

import com.example.smartmeetingroom.dto.asset.AssetDTO;

public interface AssetService {

    public void addAsset(AssetDTO dto);

    public void deleteAsset(Long assetId, Long meetingRoomId);
}
