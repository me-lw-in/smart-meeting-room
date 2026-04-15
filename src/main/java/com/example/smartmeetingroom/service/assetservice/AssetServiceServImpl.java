package com.example.smartmeetingroom.service.assetservice;

import com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO;
import com.example.smartmeetingroom.dto.assetservice.CreateAssetTicketDTO;
import com.example.smartmeetingroom.entity.AssetService;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetServiceRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.util.ConfigUtil;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AssetServiceServImpl implements AssetServiceServ {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetServiceRepository assetServiceRepository;

    @Override
    public void raiseComplaint(CreateAssetTicketDTO dto) {

        var loggedInUserId = getValidatedUser();

        // 1. find asset
        var asset = assetRepository.findById(dto.getAssetId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found.")
        );

        // 2. check if asset is working
        var workingAssetStatus = ConfigUtil.getAllowedValues("ACTIVE_ASSET_SERVICE_STATUSES").stream()
                .map(AssetStatus::valueOf)
                .collect(Collectors.toSet());
        if (!workingAssetStatus.contains(asset.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Complaint can only be raised for working assets.");
        }

        // 2. check if ticket is already raised not issue not resolved
        var activeStatuses = ConfigUtil.getAllowedValues("ACTIVE_ASSET_SERVICE_STATUSES").stream()
                .map(AssetServiceStatus::valueOf)
                .collect(Collectors.toSet());

        var isExists = assetServiceRepository.existsByAssetIdAndStatusIn(dto.getAssetId(), activeStatuses);
        if (isExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This asset already has an active complaint.");
        }

        // 3. create a ticket
        var ticket = new com.example.smartmeetingroom.entity.AssetService();
        ticket.setDescription(dto.getDescription().trim());
        ticket.setAsset(asset);
        ticket.setRaisedBy(userRepository.getReferenceById(loggedInUserId));
        assetServiceRepository.save(ticket);
    }

    @Override
    public List<AssetServiceDTO> getAllAssetComplaints(AssetServiceStatus status) {
        return assetServiceRepository.getAllServiceComplaints(status);
    }

    @Transactional
    public void makeDecisionOnComplaint(Long assetServiceId, AssetServiceDTO dto) {
        var reviewedById = getValidatedUser();

        var assetService = getValidatedAssetService(assetServiceId);

        validateNotAlreadyProcessed(assetService);

        switch (dto.getDecision()) {
            case REPAIRABLE -> handleRepairable(dto, assetService);
            case NOT_REPAIRABLE -> {
                var nonRepairableStatus = ConfigUtil.getAllowedValues("NON_REPAIRABLE_ASSET_STATUSES").stream()
                        .map(AssetStatus::valueOf)
                        .collect(Collectors.toSet());
                handleNotRepairable(dto, nonRepairableStatus, assetService);
            }
            case REJECT -> handleReject(dto, assetService);
        }
        updateAuditFields(dto, assetService, reviewedById);
    }

    private void updateAuditFields(AssetServiceDTO dto, AssetService assetService, Long reviewedById) {
        if (dto.getRemark() != null && !dto.getRemark().isBlank()) {
            assetService.setRemark(dto.getRemark());
        }
        assetService.setReviewedBy(userRepository.getReferenceById(reviewedById));
        assetService.setReviewedAt(LocalDateTime.now());
    }

    private static void handleReject(AssetServiceDTO dto, AssetService assetService) {
        if (dto.getScheduledDate() != null || dto.getAssetStatus() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No extra fields allowed for rejection");
        assetService.setDecision(dto.getDecision());
        assetService.setStatus(AssetServiceStatus.REJECTED);
    }

    private static void handleNotRepairable(AssetServiceDTO dto, Set<AssetStatus> NON_REPAIRABLE_STATUSES, AssetService assetService) {
        if (dto.getAssetStatus() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset status is required");
        else if (!NON_REPAIRABLE_STATUSES.contains(dto.getAssetStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid asset status for non-repairable case");
        }
        if (dto.getScheduledDate() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled date should not be provided");

        assetService.setDecision(dto.getDecision());
        assetService.getAsset().setStatus(dto.getAssetStatus());
        assetService.setStatus(AssetServiceStatus.CLOSED);
    }

    private static void handleRepairable(AssetServiceDTO dto, AssetService assetService) {
        if (dto.getScheduledDate() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled date is required");

        if (dto.getAssetStatus() != null)
            if (dto.getAssetStatus() == AssetStatus.UNDER_MAINTENANCE)
                assetService.getAsset().setStatus(AssetStatus.UNDER_MAINTENANCE);
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only UNDER_MAINTENANCE allowed for repairable assets");

        assetService.setDecision(dto.getDecision());
        assetService.setScheduledDate(dto.getScheduledDate());
        assetService.setStatus(AssetServiceStatus.SCHEDULED);
    }

    private static void validateNotAlreadyProcessed(AssetService assetService) {
        if (assetService.getStatus() != AssetServiceStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Complaint already processed");
        }
    }

    @NonNull
    private AssetService getValidatedAssetService(Long assetServiceId) {
        return assetServiceRepository.findById(assetServiceId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint doesnt exists.")
        );
    }

    private static Long getValidatedUser() {
        var reviewedById = SecurityUtil.getCurrentUserId();
        if (reviewedById == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        return reviewedById;
    }
}
