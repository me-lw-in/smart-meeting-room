package com.example.smartmeetingroom.service.assetservice;

import com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO;
import com.example.smartmeetingroom.dto.assetservice.CreateAssetTicketDTO;
import com.example.smartmeetingroom.dto.technician.CompleteTaskRequestDTO;
import com.example.smartmeetingroom.entity.AssetService;
import com.example.smartmeetingroom.entity.User;
import com.example.smartmeetingroom.enums.AssetServiceDecision;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.enums.UserStatus;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.AssetServiceRepository;
import com.example.smartmeetingroom.repository.UserRepository;
import com.example.smartmeetingroom.service.email.EmailService;
import com.example.smartmeetingroom.util.ConfigUtil;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AssetServiceServImpl implements AssetServiceServ {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetServiceRepository assetServiceRepository;
    private final EmailService emailService;

    @Override
    public void raiseComplaint(CreateAssetTicketDTO dto) {

        var loggedInUserId = getValidatedUser();

        // 1. find asset
        var asset = assetRepository.findById(dto.getAssetId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found.")
        );

        // 2. check if asset is working
        var workingAssetStatus = ConfigUtil.getAllowedValues("WORKING_ASSET_STATUSES").stream()
                .map(AssetStatus::valueOf)
                .collect(Collectors.toSet());
        if (!workingAssetStatus.contains(asset.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Complaint can only be raised for working assets.");
        }

        // 2. check if complaint is already raised, and it is not resolved nor rejected
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

    @Transactional
    public void startAssetService(Long assetServiceId) {

        var assetService = assetServiceRepository.findById(assetServiceId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found.")
        );

        if (assetService.getStatus() != AssetServiceStatus.ASSIGNED){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service has already started.");
        }
        var currentDate = LocalDate.now();
        if (currentDate.isBefore(assetService.getScheduledDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start service before scheduled date.");
        }

        // update asset status
        var asset = assetService.getAsset();
        asset.setStatus(AssetStatus.UNDER_MAINTENANCE);

        // update service status
        assetService.setStatus(AssetServiceStatus.IN_PROGRESS);
        assetService.setStartedAt(LocalDateTime.now());
    }

    @Transactional
    public void completeAssetService(Long assetServiceId, CompleteTaskRequestDTO dto) {
        var technicianId = SecurityUtil.getCurrentUserId();
        var isTechnician = "TECHNICIAN".equalsIgnoreCase(SecurityUtil.getCurrentUserRole());

        if (technicianId == null || !isTechnician) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        var assetService = assetServiceRepository.findById(assetServiceId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found.")
        );

        if (!assetService.getTechnician().getId().equals(technicianId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this task.");
        }

        if (assetService.getStatus() != AssetServiceStatus.IN_PROGRESS){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only in-progress tasks can be completed.");
        }

        if (dto.getServiceStatus() != AssetServiceStatus.RESOLVED &&
                dto.getServiceStatus() != AssetServiceStatus.FAILED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You can only set status as RESOLVED or FAILED."
            );
        }

        var technician = userRepository.findById(technicianId).orElseThrow(() -> new RuntimeException("Technician not found"));

        switch (dto.getServiceStatus()) {
            case RESOLVED -> handleResolvedTask(dto, assetService);

            case FAILED -> handleFailedTask(dto, assetService);
        }

        technician.setStatus(UserStatus.AVAILABLE);
    }

    public void handleResolvedTask(CompleteTaskRequestDTO dto, AssetService assetService) {
        assetService.setStatus(AssetServiceStatus.RESOLVED);
        assetService.getAsset().setStatus(AssetStatus.AVAILABLE);
        if (dto.getRemarks() != null){
            assetService.setRemark(dto.getRemarks());
        }
        assetService.setCompletedAt(LocalDateTime.now());

        // email
        String subject = "Service Completed Successfully (Complaint ID: " + assetService.getId() + ")";
        String body = """
        Hello,
        
        The service task has been completed successfully.

        Details:
        - Complaint ID: %d
        - Asset: %s
        - Status: RESOLVED
        - Completed At: %s

        Regards, \s
        Smart Meeting Room System
        """.formatted(
                assetService.getId(),
                assetService.getAsset().getAssetName(),
                assetService.getCompletedAt()
        );

        List<String> cc = new ArrayList<>();

        if (assetService.getReviewedBy() != null) {
            cc.add(assetService.getReviewedBy().getEmail());
        }
        emailService.sendEmail(
                assetService.getRaisedBy().getEmail(),
                cc,
                null,
                subject,
                body
        );
    }

    public void handleFailedTask(CompleteTaskRequestDTO dto, AssetService assetService) {
        if (dto.getRemarks() == null || dto.getRemarks().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remarks is mandatory.");
        }
        assetService.setStatus(AssetServiceStatus.FAILED);
        assetService.getAsset().setStatus(AssetStatus.INACTIVE);
        assetService.setRemark(dto.getRemarks());
        assetService.setCompletedAt(LocalDateTime.now());

        // email
        String subject = "Service Completed - Issue Not Resolved (Complaint ID: " + assetService.getId() + ")";
        String body = """
        Hello,
        
        The service task has been completed, but the issue could not be resolved.

        Details:
        - Complaint ID: %d
        - Asset: %s
        - Status: FAILED
        - Completed At: %s

        Remarks:
        %s

        Regards, \s
        Smart Meeting Room System
        """.formatted(
                assetService.getId(),
                assetService.getAsset().getAssetName(),
                assetService.getCompletedAt(),
                dto.getRemarks()
        );

        List<String> cc = new ArrayList<>();

        if (assetService.getReviewedBy() != null) {
            cc.add(assetService.getReviewedBy().getEmail());
        }
        emailService.sendEmail(
                assetService.getRaisedBy().getEmail(),
                cc,
                null,
                subject,
                body
        );
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
        assetService.setDecision(AssetServiceDecision.REJECT);
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

        assetService.setDecision(AssetServiceDecision.NOT_REPAIRABLE);
        assetService.getAsset().setStatus(dto.getAssetStatus());
        assetService.setStatus(AssetServiceStatus.RESOLVED);
    }

    private void handleRepairable(AssetServiceDTO dto, AssetService assetService) {
        if (dto.getScheduledDate() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled date is required");

        if (dto.getAssetStatus() != null) {
            if (dto.getAssetStatus() == AssetStatus.DAMAGED) //
                assetService.getAsset().setStatus(AssetStatus.DAMAGED);
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 'DAMAGED' status is allowed for repairable assets");
        }

        User technician;
        if (dto.getTechnicianId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician id is required.");
        } else {
            technician = userRepository.findByIdAndRoles_Id(dto.getTechnicianId(), (byte) 4).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technician doesnt exists.")
            );
            if (technician.getStatus() == UserStatus.ASSIGNED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Technician is already assigned.");
            } else {
                technician.setStatus(UserStatus.ASSIGNED);
                assetService.setTechnician(technician);

            }
        }
        assetService.setDecision(AssetServiceDecision.REPAIRABLE);
        assetService.setScheduledDate(dto.getScheduledDate());
        assetService.setStatus(AssetServiceStatus.ASSIGNED);

        // email
        String subject = "New Service Task Assigned (Complaint ID: " + assetService.getId() + ")";
        String body = """
        Hello,
        
        A new service task has been assigned to you.
        
        Details:
        - Complaint ID: %d
        - Asset: %s
        - Location: %s
        - Scheduled Date: %s
        
        Please log in to the system and start the task on time.
        
        Regards,
        Smart Meeting Room System
        """.formatted(
                        assetService.getId(),
                        assetService.getAsset().getAssetName(),
                        assetService.getAsset().getRoom().getRoomName(),
                        assetService.getScheduledDate()
                );

        emailService.sendEmail(
                technician.getEmail(),
                null,
                null,
                subject,
                body
        );
    }

    private static void validateNotAlreadyProcessed(AssetService assetService) {
        if (assetService.getStatus() != AssetServiceStatus.NEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Decision is already taken.");
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
