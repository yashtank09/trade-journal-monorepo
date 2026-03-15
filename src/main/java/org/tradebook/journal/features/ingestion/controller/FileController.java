package org.tradebook.journal.features.ingestion.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tradebook.journal.common.dto.DataApiResponse;
import org.tradebook.journal.config.security.AppUserDetails;
import org.tradebook.journal.features.ingestion.dto.request.FileUploadRequest;
import org.tradebook.journal.features.ingestion.dto.response.FileUploadResponse;
import org.tradebook.journal.features.ingestion.entity.FileProcessor;
import org.tradebook.journal.features.ingestion.service.FileProcessService;

import static org.tradebook.journal.common.constants.ApiConstants.*;
import static org.tradebook.journal.common.constants.IngestionConstants.*;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileProcessService storageService;

    public FileController(FileProcessService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<DataApiResponse<FileUploadResponse>> uploadFile(@Parameter(description = "Metadata for the file", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestPart("file-metadata") FileUploadRequest fileRequest, @AuthenticationPrincipal AppUserDetails userDetails, @RequestPart("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(DataApiResponse.error(CODE_BAD_REQUEST, MSG_FILE_MISSING));
        }

        Long userId = userDetails.getId();

        FileProcessor fileProcessor = storageService.uploadFile(userId, fileRequest, file);

        FileUploadResponse response = new FileUploadResponse(fileProcessor.getOriginalFileName(), fileProcessor.getFileType(), fileProcessor.getStoredPath(), fileProcessor.getStatus(), fileProcessor.getCreatedAt(), fileProcessor.getUpdatedAt());

        return ResponseEntity.ok(DataApiResponse.success(CODE_SUCCESS, MSG_FILE_UPLOAD_SUCCESS, response));
    }
}
