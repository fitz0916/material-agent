package com.material.agent.service;

import com.material.agent.tool.DocumentSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EtlPipelineService {

    private final VectorStore vectorStore;
    private final MinioClient minioClient;
    private final String bucketName;

    public EtlPipelineService(VectorStore vectorStore, MinioClient minioClient) {
        this.vectorStore = vectorStore;
        this.minioClient = minioClient;
        this.bucketName = "materials";
    }

    /**
     * 文档入库管道
     */
    public void ingestDocument(String filePath, Long materialId) {
        try {
            // 1. 从 MinIO 读取文件
            String content = readFileContent(filePath);
            
            // 2. 文档分块
            List<String> chunks = splitIntoChunks(content, 500);
            
            // 3. 向量化入库
            for (int i = 0; i < chunks.size(); i++) {
                Document doc = new Document(
                    chunks.get(i),
                    Map.of(
                        "material_id", materialId.toString(),
                        "chunk_index", i,
                        "source", filePath
                    )
                );
                vectorStore.add(List.of(doc));
            }
            
            log.info("文档入库完成: {} ({} chunks)", filePath, chunks.size());
            
        } catch (Exception e) {
            log.error("文档入库失败: {}", e.getMessage());
            throw new RuntimeException("ETL失败: " + e.getMessage());
        }
    }

    private String readFileContent(String filePath) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
            .bucket(bucketName)
            .object(filePath)
            .build();
        
        try (var stream = minioClient.getObject(args);
             var reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private List<String> splitIntoChunks(String content, int chunkSize) {
        List<String> chunks = new java.util.ArrayList<>();
        String[] words = content.split("\\s+");
        StringBuilder chunk = new StringBuilder();
        
        for (String word : words) {
            if (chunk.length() + word.length() > chunkSize) {
                chunks.add(chunk.toString());
                chunk = new StringBuilder();
            }
            chunk.append(word).append(" ");
        }
        
        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }
        
        return chunks;
    }
}
