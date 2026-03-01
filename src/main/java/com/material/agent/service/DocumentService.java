package com.material.agent.service;

import com.material.agent.model.Document;
import com.material.agent.model.Material;
import com.material.agent.repository.DocumentRepository;
import com.material.agent.repository.MaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文档管理服务
 */
@Slf4j
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MaterialRepository materialRepository;
    private final EtlPipelineService etlPipelineService;

    public DocumentService(DocumentRepository documentRepository,
                          MaterialRepository materialRepository,
                          EtlPipelineService etlPipelineService) {
        this.documentRepository = documentRepository;
        this.materialRepository = materialRepository;
        this.etlPipelineService = etlPipelineService;
    }

    /**
     * 上传文档
     */
    @Transactional
    public Document uploadDocument(MultipartFile file, String title, String category, Long materialId) throws IOException {
        // 保存文件到存储
        String filePath = saveFile(file);
        
        // 创建文档记录
        Document document = new Document();
        document.setTitle(title);
        document.setContent(new String(file.getBytes()));
        document.setCategory(category);
        document.setFileType(file.getContentType());
        document.setFilePath(filePath);
        document.setMaterialId(materialId);
        document.setStatus("active");
        document.setVectorStatus("pending");
        
        Document saved = documentRepository.save(document);
        
        // 异步向量化
        try {
            etlPipelineService.ingestDocument(filePath, materialId);
            saved.setVectorStatus("indexed");
            documentRepository.save(saved);
        } catch (Exception e) {
            log.error("文档向量化失败: {}", e.getMessage());
            saved.setVectorStatus("failed");
            documentRepository.save(saved);
        }
        
        return saved;
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + extension;
        
        // TODO: 实际保存到 MinIO 或本地存储
        // 这里返回虚拟路径
        return "documents/" + fileName;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) return "";
        return fileName.substring(lastIndexOf);
    }

    /**
     * 获取文档详情
     */
    public Optional<Document> getDocument(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * 获取物资关联文档
     */
    public List<Document> getDocumentsByMaterial(Long materialId) {
        return documentRepository.findByMaterialId(materialId);
    }

    /**
     * 获取所有文档
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * 搜索文档
     */
    public List<Document> searchDocuments(String keyword) {
        return documentRepository.searchByKeyword(keyword);
    }

    /**
     * 删除文档
     */
    @Transactional
    public void deleteDocument(Long id) {
        documentRepository.findById(id).ifPresent(doc -> {
            doc.setStatus("deleted");
            doc.setVectorStatus("removed");
            documentRepository.save(doc);
        });
    }

    /**
     * 重新索引文档
     */
    @Transactional
    public void reindexDocument(Long id) {
        documentRepository.findById(id).ifPresent(doc -> {
            try {
                etlPipelineService.ingestDocument(doc.getFilePath(), doc.getMaterialId());
                doc.setVectorStatus("indexed");
            } catch (Exception e) {
                log.error("文档重新索引失败: {}", e.getMessage());
                doc.setVectorStatus("failed");
            }
            documentRepository.save(doc);
        });
    }

    /**
     * 获取文档统计
     */
    public DocumentStats getStats() {
        long total = documentRepository.count();
        long indexed = documentRepository.findByVectorStatus("indexed").size();
        long pending = documentRepository.findByVectorStatus("pending").size();
        long failed = documentRepository.findByVectorStatus("failed").size();
        
        return new DocumentStats(total, indexed, pending, failed);
    }

    public record DocumentStats(long total, long indexed, long pending, long failed) {}
}
