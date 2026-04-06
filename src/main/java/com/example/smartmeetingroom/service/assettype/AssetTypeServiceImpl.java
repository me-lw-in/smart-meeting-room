package com.example.smartmeetingroom.service.assettype;

import com.example.smartmeetingroom.dto.assettype.AssetTypeDTO;
import com.example.smartmeetingroom.entity.AssetType;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetTypeRepository;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class AssetTypeServiceImpl implements AssetTypeService {

    private final AssetTypeRepository assetTypeRepository;
    private final AssetRepository assetRepository;

    public void addAssetType(AssetTypeDTO dto){
        String assetTypeName = StringCapitalizeUtil.capitalizeEachWord(dto.getAssetType());
        if (assetTypeRepository.existsByName(assetTypeName)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, assetTypeName + " already exists");
        }

        var assetType = new AssetType();
        assetType.setName(assetTypeName);
        assetTypeRepository.save(assetType);
    }

    public List<AssetType> getAllAssetTypes() {
        return assetTypeRepository.findAll();
    }

    public void deleteAssetType(Short id) {
        AssetType assetType = assetTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Type not found"));

        if (assetRepository.existsByAssetType_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete. Assets exist for this type.");
        }
        assetTypeRepository.delete(assetType);
    }

    @Transactional
    public void updateAssetType(Short id, AssetTypeDTO dto) {
        String assetTypeName = StringCapitalizeUtil.capitalizeEachWord(dto.getAssetType());
        var assetType = assetTypeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "Asset type doesn't exists.")
        );
        assetType.setName(assetTypeName);
    }
}