package com.example.smartmeetingroom.service.assetservice;

import com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO;
import com.example.smartmeetingroom.dto.assetservice.CreateAssetTicketDTO;
import com.example.smartmeetingroom.enums.AssetServiceStatus;

import java.util.List;

public interface AssetServiceServ {

    public void raiseComplaint(CreateAssetTicketDTO dto);

    public List<AssetServiceDTO> getAllAssetComplaints(AssetServiceStatus status);

    public void makeDecisionOnComplaint(Long assetServiceId, AssetServiceDTO dto);
}
