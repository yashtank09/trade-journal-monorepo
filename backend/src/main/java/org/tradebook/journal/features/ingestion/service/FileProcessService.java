package org.tradebook.journal.features.ingestion.service;

import org.springframework.web.multipart.MultipartFile;
import org.tradebook.journal.features.ingestion.dto.request.FileUploadRequest;
import org.tradebook.journal.features.ingestion.entity.FileProcessor;

public interface FileProcessService {
    FileProcessor uploadFile(Long userId, FileUploadRequest fileRequest, MultipartFile file);
}
