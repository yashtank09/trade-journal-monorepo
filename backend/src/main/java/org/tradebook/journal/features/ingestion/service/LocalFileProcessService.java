package org.tradebook.journal.features.ingestion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tradebook.journal.config.StorageProperties;
import org.tradebook.journal.features.ingestion.dto.request.FileUploadRequest;
import org.tradebook.journal.features.ingestion.entity.FileProcessor;
import org.tradebook.journal.common.exception.FileStorageExcpetion;
import org.tradebook.journal.common.exception.FileValidationException;
import org.tradebook.journal.features.ingestion.repository.FileJobProcessRepository;
import org.tradebook.journal.common.enums.JobStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocalFileProcessService implements FileProcessService {

    private final FileJobProcessRepository repository;
    private final StorageProperties storageProperties;

    @Autowired
    public LocalFileProcessService(FileJobProcessRepository repository, StorageProperties storageProperties) {
        this.repository = repository;
        this.storageProperties = storageProperties;
    }

    /**
     * File validation logic
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("Uploaded file is empty");
        }

        // Add file size check (e.g., 10MB limit)
        long maxFileSize = 10L * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new FileValidationException("File size exceeds maximum limit of 10MB");
        }

        // Rest of the validation
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new FileValidationException("Invalid file name");
        }

        String fileType = fileName.split("\\.")[1];
        List<String> supportedTypes = storageProperties.getSupportedTypes();
        if (!supportedTypes.contains(fileType)) {
            throw new FileValidationException("Unsupported file type: " + fileType);
        }
    }

    @Override
    public FileProcessor uploadFile(Long userId, FileUploadRequest fileRequest, MultipartFile file) {
        validateFile(file);
        try {
            Files.createDirectories(Paths.get(storageProperties.getUploadDir()));

            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path destination = Paths.get(storageProperties.getUploadDir()).resolve(storedFileName);

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            FileProcessor job = new FileProcessor();
            job.setUserId(userId);
            job.setOriginalFileName(file.getOriginalFilename());
            job.setStoredFileName(storedFileName);
            job.setStoredPath(destination.toString());
            job.setStatus(JobStatus.PENDING);
            job.setFileSize(file.getSize());
            job.setFileCategory(fileRequest.getFileCategory());
            job.setFileType(file.getContentType());
            job.setCreatedAt(LocalDateTime.now());

            return repository.save(job);

        } catch (IOException e) {
            throw new FileStorageExcpetion("File storage failed", e);
        }
    }
}
