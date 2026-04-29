package com.example.smartmeetingroom.service.cache;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryProcedureCacheService implements ProcedureCacheService{

    private final Map<Long, List<ProcedureDTO>> cache = new ConcurrentHashMap<>();

    @Override
    public List<ProcedureDTO> get(Long assetId) {
        return cache.get(assetId);
    }

    @Override
    public void put(Long assetId, List<ProcedureDTO> procedures) {
        cache.put(assetId, procedures);
    }

    @Override
    public void evict(Set<Long> assetIds) {
        for (var assetId : assetIds) {
            cache.remove(assetId);
        }
    }
}
