package com.example.smartmeetingroom.service.asset;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface AssetService {

    public void addAsset(AssetDTO dto);

    public void deleteAsset(Long assetId, Long meetingRoomId);

    public PageResponseDTO<AssetDTO> getAllAssets(int page,
                                                   int size,
                                                   String search,
                                                   Short typeId,
                                                   Long meetingRoomId,
                                                   AssetStatus status,
                                                   List<String> sortParam);

    public void updateAsset(Long assetId, JsonNode request);
}
