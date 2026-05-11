package com.example.smartmeetingroom.service.cache;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Primary
@Service
@RequiredArgsConstructor
public class RedisProcedureCacheService
        implements ProcedureCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "asset_procedures:";

    @Override
    public List<ProcedureDTO> get(Long assetId) {
        return (List<ProcedureDTO>) redisTemplate.opsForValue().get(PREFIX + assetId);
    }

    @Override
    public void put(Long assetId, List<ProcedureDTO> procedures) {
        redisTemplate.opsForValue().set(PREFIX + assetId, procedures);
    }

    @Override
    public void evict(Set<Long> assetIds) {
        for (Long assetId : assetIds) {
            redisTemplate.delete(PREFIX + assetId);
        }
    }
}