package com.example.smartmeetingroom.service.assettype;

import com.example.smartmeetingroom.dto.assettype.AssetTypeDTO;
import com.example.smartmeetingroom.entity.AssetType;

import java.util.List;

public interface AssetTypeService {

    public void addAssetType(AssetTypeDTO dto);

    public List<AssetType> getAllAssetTypes();

    public void deleteAssetType(Short id);

    public void updateAssetType(Short id, AssetTypeDTO dto);
}
