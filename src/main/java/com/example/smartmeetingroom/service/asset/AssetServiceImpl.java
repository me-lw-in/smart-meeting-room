package com.example.smartmeetingroom.service.asset;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetTypeRepository;
import com.example.smartmeetingroom.repository.MeetingRoomRepository;
import com.example.smartmeetingroom.specification.AssetSpecification;
import com.example.smartmeetingroom.util.StringCapitalizeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "assetName",
            "serialNumber",
            "purchaseDate",
            "warrantyExpiry"
    );

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

    public PageResponseDTO<AssetDTO> getAllAssets(int page,
                                    int size,
                                    String search,
                                    Short typeId,
                                    Long meetingRoomId,
                                    AssetStatus status,
                                    List<String> sortParam) {
        var specification = AssetSpecification.getAssets(search,typeId,meetingRoomId,status);
        var orders = getOrders(sortParam);
        Sort sort = Sort.by(orders);
        Pageable pageable = PageRequest.of(page, size, sort);

        var content =  assetRepository.findAll(specification, pageable);
        return getAssetDTOPageResponseDTO(content);
    }


    private static PageResponseDTO<AssetDTO> getAssetDTOPageResponseDTO(Page<Asset> content) {
        Page<AssetDTO> dtoPage = content.map(a -> {
            var asset = new AssetDTO();
            asset.setAssetId(a.getId());
            asset.setAssetName(a.getAssetName());
            asset.setSerialNumber(a.getSerialNumber());
            asset.setPurchaseDate(a.getPurchaseDate());
            asset.setWarrantyExpiry(a.getWarrantyExpiry());
            asset.setMeetingRoomName(a.getRoom().getRoomName());
            asset.setAssetTypeName(a.getAssetType().getName());
            return asset;
        });
        var pageResponse = new PageResponseDTO<AssetDTO>();
        pageResponse.setContent(dtoPage.getContent());
        pageResponse.setPage(dtoPage.getNumber());
        pageResponse.setSize(dtoPage.getSize());
        pageResponse.setTotalPages(dtoPage.getTotalPages());
        pageResponse.setTotalElements(dtoPage.getTotalElements());
        return pageResponse;
    }

    private static List<Sort.Order> getOrders(List<String> sortParam) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sortParam != null && !sortParam.isEmpty()) {
            for (String sortValue : sortParam) {
                if (!sortValue.isBlank()) {
                    var sortValueParts = sortValue.split(",");
                    var field = sortValueParts[0].trim();
                    var direction = sortValueParts[1].trim().length() > 1 ? sortValueParts[1].trim() : "asc";
                    if (!ALLOWED_SORT_FIELDS.contains(field)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field: " + field);
                    }
                    if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort direction: " + direction);
                    }
                    Sort.Order order = direction.equalsIgnoreCase("asc") ? new Sort.Order(Sort.Direction.ASC, field) : new Sort.Order(Sort.Direction.DESC, field);
                    orders.add(order);
                }
            }
        }else {
            orders.add(new Sort.Order(Sort.Direction.ASC, "purchaseDate"));
        }
        if (orders.isEmpty()){
            orders.add(new Sort.Order(Sort.Direction.ASC, "purchaseDate"));
        }
        return orders;
    }
}
