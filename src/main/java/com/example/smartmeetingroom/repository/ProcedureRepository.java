package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.entity.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
}
