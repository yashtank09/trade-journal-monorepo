package org.tradebook.journal.features.ingestion.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.tradebook.journal.features.ingestion.entity.FileProcessor;
import org.tradebook.journal.common.enums.JobStatus;

import java.util.List;


public interface FileJobProcessRepository extends JpaRepository<FileProcessor, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FileProcessor f WHERE f.status = ?1 ORDER BY f.createdAt ASC")
    List<FileProcessor> findPendingFiles(JobStatus status, Pageable pageable);
}
