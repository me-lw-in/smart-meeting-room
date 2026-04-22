package com.example.smartmeetingroom.dto.technician;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.enums.AssetStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TechnicianTaskResponseDTO {
    private Long complaintId;
    private String description;
    private LocalDate scheduledDate;
    private AssetServiceStatus status;

    private Long assetId;
    private String assetName;
    private String assetType;
    private String serialNumber;
    private AssetStatus assetStatus;

    private String meetingRoomName;

    private List<ProcedureDTO> procedures;

    private LocalDateTime assignedAt;

    public TechnicianTaskResponseDTO(
            Long complaintId,
            String description,
            LocalDate scheduledDate,
            AssetServiceStatus status,
            Long assetId,
            String assetName,
            String assetType,
            String serialNumber,
            AssetStatus assetStatus,
            String meetingRoomName,
            LocalDateTime assignedAt
    ) {
        this.complaintId = complaintId;
        this.description = description;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetType = assetType;
        this.serialNumber = serialNumber;
        this.assetStatus = assetStatus;
        this.meetingRoomName = meetingRoomName;
        this.assignedAt = assignedAt;
    }
}
