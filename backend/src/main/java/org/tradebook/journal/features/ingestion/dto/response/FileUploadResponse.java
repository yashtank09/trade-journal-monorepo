package org.tradebook.journal.features.ingestion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tradebook.journal.common.enums.JobStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String fileType;
    private String storedPath;
    private JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
