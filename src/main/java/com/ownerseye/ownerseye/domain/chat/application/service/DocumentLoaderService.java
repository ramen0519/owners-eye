package com.ownerseye.ownerseye.domain.chat.application.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoaderService {

    private final VectorStore vectorStore;

    private static final String[] DOCUMENT_FILES = {
            "documents/baemin-ad-guide.txt",
            "documents/baemin-settlement-guide.txt",
            "documents/coupang-settlement-guide.txt"
    };

    @PostConstruct
    public void loadDocuments() {
        List<Document> documents = new ArrayList<>();

        for (String filePath : DOCUMENT_FILES) {
            try {
                ClassPathResource resource = new ClassPathResource(filePath);
                if (!resource.exists()) continue;

                String content;
                try (var inputStream = resource.getInputStream()) {
                    content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
                }
                if (content.isEmpty()) continue;

                documents.add(new Document(content, Map.of("source", filePath)));
                log.info("문서 로드: {}", filePath);
            } catch (IOException e) {
                log.warn("문서 로드 실패: {}", filePath);
            }
        }

        if (documents.isEmpty()) {
            log.warn("RAG 문서 없음. src/main/resources/documents/ 에 txt 파일을 추가하세요.");
            return;
        }

        try {
            TokenTextSplitter splitter = new TokenTextSplitter();
            vectorStore.add(splitter.apply(documents));
            log.info("벡터 저장소에 문서 {} 개 저장 완료", documents.size());
        } catch (Exception e) {
            log.error("벡터 저장소 문서 저장 실패. RAG 기능이 비활성화됩니다.", e);
        }
    }
}
