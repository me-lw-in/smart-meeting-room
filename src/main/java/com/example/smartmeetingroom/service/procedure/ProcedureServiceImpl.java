package com.example.smartmeetingroom.service.procedure;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.dto.procedure.UpdateProcedureRequestDTO;
import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.entity.Procedure;
import com.example.smartmeetingroom.repository.AssetRepository;
import com.example.smartmeetingroom.repository.ProcedureRepository;
import com.example.smartmeetingroom.service.cache.ProcedureCacheService;
import com.example.smartmeetingroom.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ProcedureServiceImpl implements  ProcedureService{

    private final ProcedureRepository procedureRepository;
    private final AssetRepository assetRepository;
    private final ProcedureCacheService cacheService;

    @Override
    public void createProcedure(ProcedureDTO request) {

        var procedure = new Procedure();
        procedure.setTitle(request.getTitle());
        procedure.setProcedureText(request.getSteps());

        procedureRepository.save(procedure);
        log.info("User with id - {}, added a procedure with id - {}", SecurityUtil.getCurrentUserId(), procedure.getId());
    }

    @Override
    public Page<ProcedureDTO> getAllProcedures(int page, int size, String search) {

        var pageable = PageRequest.of(page, size);
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
    public void linkAssetsToProcedure(Long procedureId, List<Long> newAssetIds) {

        var procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedure not found"));

        List<Asset> assets = assetRepository.findAllById(newAssetIds);

        if (assets.size() != newAssetIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some assets not found");
        }

        // Collect old asset ids
        Set<Long> oldAssetIds = procedure.getAssets().stream()
                .map(Asset::getId)
                .collect(Collectors.toSet());

        // check removedIds
        Set<Long> removedIds = new HashSet<>(oldAssetIds);
        removedIds.removeAll(newAssetIds);

        // check added
        Set<Long> addedIds = new HashSet<>(newAssetIds);
        addedIds.removeAll(oldAssetIds);

        procedure.getAssets().clear();
        procedure.getAssets().addAll(assets);

        if (!removedIds.isEmpty()) {
            cacheService.evict(removedIds);
        }
        if (!addedIds.isEmpty()) {
            cacheService.evict(addedIds); // 🔥 IMPORTANT: don't put stale data
        }
        log.info("User with id - {}, linked / updated assets to procedure with id - {}", SecurityUtil.getCurrentUserId(), procedure.getId());
    }

    @Override
    @Transactional
    public void updateProcedure(Long procedureId, UpdateProcedureRequestDTO request) {

        var procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedure not found"));

        if (request.getTitle() != null) {
            procedure.setTitle(request.getTitle());
        }

        if (request.getProcedureText() != null) {
            procedure.setProcedureText(request.getProcedureText());
        }
        
        var assetIds = procedureRepository.findAssetIdsByProcedureId(procedureId);
        cacheService.evict(assetIds);
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

        procedureRepository.delete(procedure);
    }
}
