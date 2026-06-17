package org.tradebook.journal.features.ingestion.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tradebook.journal.common.enums.FileCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
//    @JsonProperty("file")
//    private MultipartFile file;
    @JsonProperty("file-type")
    private String fileType;      // CSV, EXCEL, JSON
    @JsonProperty("source-system")
    private String sourceSystem;  // UI, API, CRON
    @JsonProperty("description")
    private String description;
    @JsonProperty("file-category")
    private FileCategory fileCategory;
}
