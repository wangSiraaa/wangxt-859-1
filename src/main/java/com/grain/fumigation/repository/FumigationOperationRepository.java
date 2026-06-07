package com.grain.fumigation.repository;

import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.enums.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FumigationOperationRepository extends JpaRepository<FumigationOperation, Long> {

    Optional<FumigationOperation> findByTicketNo(String ticketNo);

    List<FumigationOperation> findByStatus(OperationStatus status);

    List<FumigationOperation> findByKeeperId(String keeperId);

    List<FumigationOperation> findByWarehouseCode(String warehouseCode);

    boolean existsByTicketNo(String ticketNo);
}
