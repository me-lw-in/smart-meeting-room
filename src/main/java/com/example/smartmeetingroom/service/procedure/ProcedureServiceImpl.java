package com.example.smartmeetingroom.service.procedure;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.audit.AuditEventDTO;
import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.dto.procedure.UpdateProcedureRequestDTO;
import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.entity.Procedure;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.ProcedureRepository;
import com.example.smartmeetingroom.repository.RoleRepository;
import com.example.smartmeetingroom.service.cache.ProcedureCacheService;
import com.example.smartmeetingroom.service.producer.ProducerService;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Slf4j
@Service
@AllArgsConstructor
public class ProcedureServiceImpl implements  ProcedureService{

    private final AssetRepository assetRepository;
    private final ProducerService auditService;
    private final ProcedureRepository procedureRepository;
    private final ProcedureCacheService cacheService;
    private final RoleRepository roleRepository;

    @Override
    public void createProcedure(ProcedureDTO request) {

        var procedure = new Procedure();
        procedure.setTitle(request.getTitle());
        procedure.setProcedureText(request.getSteps());

        procedureRepository.save(procedure);
        auditService.sendAuditEvent(new AuditEventDTO(
                "ADD_PROCEDURE",
                "PROCEDURES",
                procedure.getId(),
                SecurityUtil.getCurrentUserId(),
                roleRepository.findByRoleName(SecurityUtil.getCurrentUserRole()).get().getId(),
                "ADD NEW PROCEDURE",
                LocalDateTime.now()
        ));
        log.info("User with id - {}, added a procedure with id - {}", SecurityUtil.getCurrentUserId(), procedure.getId());
    }

    @Override
    public Page<ProcedureDTO> getAllProcedures(int page, int size, String search) {

        var pageable = PageRequest.of(page - 1, size);
        var procedurePage = procedureRepository.findAllWithAssets(search, pageable);
        return procedurePage.map(procedure -> new ProcedureDTO(
                procedure.getId(),
                procedure.getTitle(),
                procedure.getProcedureText(),
                procedure.getAssets().stream()
                        .map(asset -> new AssetDTO(
                                asset.getId(),
                                asset.getAssetName(),
                                asset.getSerialNumber()
                        )).toList()
        ));
    }

    @Override
    @Transactional
    public String linkAssetsToProcedure(Long procedureId, String action, List<Long> assetIds) {

        var procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedure not found"));

        List<Asset> assets = assetRepository.findAllById(assetIds);

        if (assets.size() != new HashSet<>(assetIds).size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some assets not found");
        }

        ProcedureDTO procedureDto = new ProcedureDTO(
                procedure.getId(),
                procedure.getTitle(),
                procedure.getProcedureText()
        );

        if ("link".equalsIgnoreCase(action)) {

            for (Asset asset : assets) {

                procedure.getAssets().add(asset);

                List<ProcedureDTO> existingList = cacheService.get(asset.getId());

                if (existingList == null) {
                    existingList = new ArrayList<>();
                }

                boolean alreadyExists = existingList.stream()
                        .anyMatch(p -> p.getId().equals(procedureId));

                if (!alreadyExists) {
                    existingList.add(procedureDto);
                }

                cacheService.put(asset.getId(), existingList);
            }

            return "linked";
        }

        else if ("unlink".equalsIgnoreCase(action)) {

            for (Asset asset : assets) {

                procedure.getAssets().remove(asset);

                List<ProcedureDTO> existingList = cacheService.get(asset.getId());

                if (existingList != null) {

                    existingList.removeIf(p -> p.getId().equals(procedureId));

                    if (existingList.isEmpty()) {
                        cacheService.evict(Set.of(asset.getId()));
                    } else {
                        cacheService.put(asset.getId(), existingList);
                    }
                }
            }

            return "unlinked";
        }

        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action");
        }
    }

    @Override
    @Transactional
    public void updateProcedure(Long procedureId, UpdateProcedureRequestDTO request) {

        var procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedure not found"));
        boolean isTitleChanaged = false;
        boolean isProcedureTextChanged = false;
        if (request.getTitle() != null) {
            isTitleChanaged = !procedure.getTitle().equalsIgnoreCase(request.getTitle());
            procedure.setTitle(request.getTitle());
        }

        if (request.getProcedureText() != null) {
            isProcedureTextChanged = !procedure.getProcedureText().equalsIgnoreCase(request.getProcedureText());
            procedure.setProcedureText(request.getProcedureText());
        }

        var assetIds = procedureRepository.findAssetIdsByProcedureId(procedureId);
        cacheService.evict(assetIds);
        String changedText;
        if (isTitleChanaged && isProcedureTextChanged) {
            changedText = "TITLE & PROCEDURE TEXT";
            auditService.sendAuditEvent(new AuditEventDTO(
                    "UPDATE_PROCEDURE",
                    "PROCEDURES",
                    procedure.getId(),
                    SecurityUtil.getCurrentUserId(),
                    roleRepository.findByRoleName(SecurityUtil.getCurrentUserRole()).get().getId(),
                    "PROCEDURE IS UPDATED (" + changedText + " changed)" ,
                    LocalDateTime.now()
            ));
        } else if (isTitleChanaged) {
            changedText = "TITLE";
            auditService.sendAuditEvent(new AuditEventDTO(
                    "UPDATE_PROCEDURE",
                    "PROCEDURES",
                    procedure.getId(),
                    SecurityUtil.getCurrentUserId(),
                    roleRepository.findByRoleName(SecurityUtil.getCurrentUserRole()).get().getId(),
                    "PROCEDURE IS UPDATED (" + changedText + " changed)" ,
                    LocalDateTime.now()
            ));
        }else if (isProcedureTextChanged){
            changedText = "PROCEDURE TEXT";
            auditService.sendAuditEvent(new AuditEventDTO(
                    "UPDATE_PROCEDURE",
                    "PROCEDURES",
                    procedure.getId(),
                    SecurityUtil.getCurrentUserId(),
                    roleRepository.findByRoleName(SecurityUtil.getCurrentUserRole()).get().getId(),
                    "PROCEDURE IS UPDATED (" + changedText + " changed)" ,
                    LocalDateTime.now()
            ));
        }

    }

    @Transactional
    public void deleteProcedure(Long procedureId) {

        var procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedure not found"));

        if (procedureRepository.existsByIdAndAssetsIsNotEmpty(procedureId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please unlink assets before deleting the procedure"
            );
        }
        auditService.sendAuditEvent(new AuditEventDTO(
                "DELETE_PROCEDURE",
                "PROCEDURES",
                procedure.getId(),
                SecurityUtil.getCurrentUserId(),
                roleRepository.findByRoleName(SecurityUtil.getCurrentUserRole()).get().getId(),
                "PROCEDURE WITH ID " + procedure.getId() + " is deleted",
                LocalDateTime.now()
        ));
        procedureRepository.delete(procedure);
    }
}
