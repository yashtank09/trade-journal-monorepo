package org.tradebook.journal.features.ingestion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.tradebook.journal.common.entity.BaseEntity;
import org.tradebook.journal.common.enums.FileCategory;
import org.tradebook.journal.common.enums.JobStatus;

@Getter
@Setter
@Entity
@Table(name = "file_jobs")
public class FileProcessor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_job_id")
    private Long fileJobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "orignal_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "stored_path", nullable = false)
    private String storedPath;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileCategory fileCategory;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

}
