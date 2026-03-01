package com.material.agent.controller;

import com.material.agent.model.Document;
import com.material.agent.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 文档管理 API
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@Tag(name = "文档管理", description = "文档上传、搜索、下载接口")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文档")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "materialId", required = false) Long materialId) {
        
        try {
            String docTitle = title != null ? title : file.getOriginalFilename();
            Document doc = documentService.uploadDocument(file, docTitle, category, materialId);
            return ResponseEntity.ok(doc);
        } catch (IOException e) {
            log.error("文档上传失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        return documentService.getDocument(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "获取所有文档")
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/material/{materialId}")
    @Operation(summary = "获取物资关联文档")
    public ResponseEntity<List<Document>> getDocumentsByMaterial(@PathVariable Long materialId) {
        return ResponseEntity.ok(documentService.getDocumentsByMaterial(materialId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索文档")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String keyword) {
        return ResponseEntity.ok(documentService.searchDocuments(keyword));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reindex")
    @Operation(summary = "重新索引文档")
    public ResponseEntity<Void> reindexDocument(@PathVariable Long id) {
        documentService.reindexDocument(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取文档统计")
    public ResponseEntity<DocumentService.DocumentStats> getStats() {
        return ResponseEntity.ok(documentService.getStats());
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "下载文档")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws IOException {
        Document doc = documentService.getDocument(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + doc.getTitle() + "\"")
                .body(resource);
    }
}
