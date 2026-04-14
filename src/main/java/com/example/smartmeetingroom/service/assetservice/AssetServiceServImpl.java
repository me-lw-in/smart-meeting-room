package com.example.smartmeetingroom.service.assetservice;

import com.example.smartmeetingroom.dto.assetservice.CreateAssetTicketDTO;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetServiceRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class AssetServiceServImpl implements AssetServiceServ {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetServiceRepository assetServiceRepository;

    @Override
    public void createTicket(CreateAssetTicketDTO dto) {
        var loggedInUserId = SecurityUtil.getCurrentUserId();
        if (loggedInUserId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        // 1. find asset
        var asset = assetRepository.findById(dto.getAssetId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found.")
        );

        // 2. check if ticket is already raised not issue not resolved
        var isExists = assetServiceRepository.existsByAssetIdAndStatusIn(dto.getAssetId(), List.of(AssetServiceStatus.OPEN, AssetServiceStatus.IN_PROGRESS));
        if (isExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This asset already has an active service request.");
        }

        // 3. create a ticket
        var ticket = new com.example.smartmeetingroom.entity.AssetService();
        ticket.setDescription(dto.getDescription().trim());
        ticket.setAsset(asset);
        ticket.setRaisedBy(userRepository.getReferenceById(loggedInUserId));
        assetServiceRepository.save(ticket);
    }
}
