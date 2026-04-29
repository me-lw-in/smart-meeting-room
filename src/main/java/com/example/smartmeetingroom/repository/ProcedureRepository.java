package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.entity.Procedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.procedure.ProcedureDTO(
            p.id,
            p.title,
            p.procedureText
        )
        FROM Procedure p
        JOIN p.assets a
        WHERE a.id = :assetsId
    """)
    List<ProcedureDTO> getAssetProcedures(Long assetsId);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.procedure.ProcedureDTO(
            p.id,
            p.title,
            p.procedureText
        )
        FROM Procedure p
        JOIN p.assets a
        WHERE a.id IN :assetsId
    """)
    List<ProcedureDTO> getProceduresForAssets(List<Long> assetsId);

    @Query("""
    SELECT DISTINCT p FROM Procedure p
    LEFT JOIN FETCH p.assets a
    WHERE (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))
""")
    Page<Procedure> findAllWithAssets(@Param("search") String search, Pageable pageable);

    boolean existsByIdAndAssetsIsNotEmpty(Long id);

    @Query(value = "SELECT asset_id FROM asset_procedures WHERE procedure_id = :procedureId", nativeQuery = true)
    Set<Long> findAssetIdsByProcedureId(@Param("procedureId") Long procedureId);
}
